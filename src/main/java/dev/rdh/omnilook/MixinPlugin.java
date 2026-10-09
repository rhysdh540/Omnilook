package dev.rdh.omnilook;

import com.mumfrey.liteloader.launch.LiteLoaderTweaker;
import cpw.mods.modlauncher.Environment;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import net.minecraftforge.versions.forge.ForgeVersion;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class MixinPlugin implements IMixinConfigPlugin {
	private static String platform;
	private static Map<String, List<String>> mixins;

	// region Reflection Utilities
	public static boolean classExists(String className) {
		try(InputStream stream = MixinService.getService().getResourceAsStream(className.replace('.', '/') + ".class")) {
			return stream != null;
		}
	}

	public static Field field(Class<?> clazz, String... names) {
		for(String name : names) {
			try {
				return clazz.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				// continue
			}
		}
		throw new NoSuchFieldException("No such field in class " + clazz.getName() + ": " + Arrays.toString(names));
	}
	// endregion

	public static String getPlatform() {
		return Objects.requireNonNull(platform, "Platform not set");
	}

	private static String launcherPlatform(Class<?> launcher) {
		for(Class<?> type = launcher; type != null; type = type.getSuperclass()) {
			String name = type.getName();
			if(name.startsWith("net.fabricmc.loader.")) return "Fabric";
			if(name.startsWith("net.neoforged.fml.")) return "NeoForge";
			if(name.startsWith("net.minecraftforge.fml.")) return "LexForge";
			if(name.startsWith("com.fox2code.foxloader.")) return "ReIndev";
		}
		throw new IllegalStateException("Unsupported launcher: " + launcher.getName());
	}

	private static String detectPlatform(Class<?> service) {
		for(Class<?> type = service; type != null; type = type.getSuperclass()) {
			String name = type.getName();
			if(name.equals("org.spongepowered.asm.service.modlauncher.MixinServiceModLauncher")) {
				Environment environment = Launcher.INSTANCE.environment();
				String target = environment.getProperty(IEnvironment.Keys.LAUNCHTARGET.get())
						.orElseThrow(() -> new IllegalStateException("Launch target not set"));
				return launcherPlatform(environment.findLaunchHandler(target)
						.orElseThrow(() -> new IllegalStateException("Launch handler not found")).getClass());
			}
			if(name.equals("org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper") ||
					name.equals("org.spongepowered.asm.service.mojang.AbstractMixinServiceLaunchWrapper")) {
				if(classExists("org.dimdev.riftloader.RiftLoader")) {
					Field loaded = field(RiftLoader.class, "loaded");
					loaded.setAccessible(true);
					if(loaded.getBoolean(RiftLoader.instance)) return "Rift";
				}
				if(classExists("com.mumfrey.liteloader.launch.LiteLoaderTweaker")) {
					Field instance = field(LiteLoaderTweaker.class, "instance"); // is private
					instance.setAccessible(true);
					if(instance.get(null) != null) return "LiteLoader";
				}
				if(classExists("net.minecraftforge.fml.relauncher.FMLLaunchHandler") &&
						net.minecraftforge.fml.relauncher.FMLLaunchHandler.side() != null) {
					return "LexForge12";
				}
				if(classExists("cpw.mods.fml.relauncher.FMLLaunchHandler") &&
						cpw.mods.fml.relauncher.FMLLaunchHandler.side() != null) {
					return "LexForge12";
				}
				throw new IllegalStateException("No initialized LaunchWrapper platform found");
			}
		}
		return launcherPlatform(service);
	}

	@SuppressWarnings({"unchecked", "DataFlowIssue", "Java8ListReplaceAll"})
	@Override
	public void onLoad(String mixinPackage) {
		if(platform != null) {
			throw new IllegalStateException("onLoad called twice");
		}

		Omnilook.LOGGER.info("initializing mixin plugin");

		platform = detectPlatform(MixinService.getService().getClass());
		if(platform.equals("LexForge")) {
			String forgeVersion = ForgeVersion.getVersion();
			int major = Integer.parseInt(forgeVersion.substring(0, forgeVersion.indexOf('.')));
			if(major >= 50) {
				// it's already set to LexForge
			} else if(major >= 37) {
				platform = "LexForge20";
			} else if(major >= 26) {
				platform = "LexForge16";
			} else if(major == 25) {
				platform = "LexForge13";
			} else {
				throw new IllegalStateException("Unexpected forge version: " + forgeVersion);
			}
		} else if(platform.equals("Fabric")) {
			if (classExists("net.minecraft.unmapped.C_76792751") ||
					(FabricLoader.getInstance().isDevelopmentEnvironment() &&
					 FabricLoader.getInstance().getMappingResolver().getNamespaces().contains("intermediary") &&
					 FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary",
						 "net.minecraft.unmapped.C_76792751").equals("net.minecraft.client.options.GameOptions"))) {
				platform = "Ornithe";
			} else if(classExists("net.minecraft.unmapped.C_8105098")) {
				throw new IllegalStateException("Omnilook requires Ornithe generation 2 intermediaries");
			} else {
				Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer("minecraft");
				if(!mod.isPresent()) {
					throw new IllegalStateException("Minecraft mod not present?");
				}
				Version version = mod.get().getMetadata().getVersion();
				int cmp = version.compareTo(Version.parse("1.21.11"));
				if(cmp <= 0) { // if > 1.21.11, is already set to Fabric
					cmp = version.compareTo(Version.parse("1.14.4"));
					if(cmp >= 0) {
						platform = "IntermediaryFabric";
					} else {
						cmp = version.compareTo(Version.parse("1.0.0-beta.7.3"));
						if(cmp > 0) {
							platform = "LegacyFabric";
						} else {
							platform = "Babric";
						}
					}
				}
			}
		}

		Omnilook.LOGGER.info("Omnilook mixin plugin detected platform: {}", platform);

		Reader raw = new InputStreamReader(MixinPlugin.class.getClassLoader().getResourceAsStream("META-INF/mixinlist.json"));
		Map<String, List<String>> mixins;
		if(classExists("org.spongepowered.include.com.google.gson.Gson")) {
			mixins = new org.spongepowered.include.com.google.gson.Gson().fromJson(raw, Map.class);
		} else if(classExists("com.google.gson.Gson")) {
			mixins = new com.google.gson.Gson().fromJson(raw, Map.class);
		} else {
			throw new IllegalStateException("Gson not found");
		}
		for(String platform : mixins.keySet()) {
			List<String> list = mixins.get(platform);
			for(int i = 0; i < list.size(); i++) {
				list.set(i, platform + "." + list.get(i));
			}
		}
		mixins.get("lexforge13").add("lexforge16.MouseHandlerMixin"); // least scuffed 1.13 hack
		MixinPlugin.mixins = Collections.unmodifiableMap(mixins);
	}

	@Override
	public List<String> getMixins() {
		if(mixins == null) {
			throw new IllegalStateException("onLoad not called");
		}
		List<String> m = Objects.requireNonNull(
				mixins.get(getPlatform().toLowerCase()),
				"Mixins not found for platform: " + platform
		);

		Omnilook.LOGGER.info("Found mixins: {}", m);
		return m;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if(mixinClassName.endsWith(".ornithe.EarlyInputMixin")) {
			return FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion()
					.compareTo(Version.parse("1.0.0-alpha.0.1.1")) < 0;
		}
		return true;
	}

	public static void noop() {}

	//region Unused
	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	// compat with old mixin (liteloader)
	@SuppressWarnings("unused")
	public void preApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@SuppressWarnings("unused")
	public void postApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
	//endregion
}
