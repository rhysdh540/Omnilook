package dev.rdh.omnilook;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraftforge.versions.forge.ForgeVersion;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
		try {
			Class.forName(className, false, MixinPlugin.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException e) {
			return false;
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

	@Override
	public void onLoad(String mixinPackage) {
		if(platform != null) {
			throw new IllegalStateException("onLoad called twice");
		}

		if(classExists("net.neoforged.fml.common.Mod")) {
			platform = "NeoForge";
		} else if(classExists("net.minecraftforge.versions.forge.ForgeVersion")) {
			String forgeVersion = ForgeVersion.getVersion();
			int major = Integer.parseInt(forgeVersion.substring(0, forgeVersion.indexOf('.')));
			if(major >= 50) {
				platform = "LexForge";
			} else if(major >= 37) {
				platform = "LexForge20";
			} else if(major >= 26) {
				platform = "LexForge16";
			} else if(major == 25) {
				platform = "LexForge13";
			} else {
				throw new IllegalStateException("Unexpected forge version: " + forgeVersion);
			}
		} else if(classExists("net.fabricmc.loader.api.FabricLoader")) {
			Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer("minecraft");
			if(!mod.isPresent()) {
				throw new IllegalStateException("Minecraft mod not present?");
			}
			Version version = mod.get().getMetadata().getVersion();
			int cmp = version.compareTo(Version.parse("1.14.4"));
			if(cmp >= 0) {
				platform = "Fabric";
			} else {
				cmp = version.compareTo(Version.parse("1.0.0-beta.7.3"));
				if(cmp > 0) {
					platform = "LegacyFabric";
				} else {
					platform = "Babric";
				}
			}
		} else if(classExists("org.dimdev.rift.Rift")) {
			platform = "Rift";
		} else if(classExists("com.mumfrey.liteloader.LiteMod")) {
			platform = "LiteLoader";
		} else if(classExists("com.fox2code.foxloader.loader.Mod")) {
			platform = "FoxLoader";
		} else if(classExists("net.minecraft.command.ICommand")) {
			// brigadier was introduced in 1.13, obsoleting the old command system
			platform = "LexForge12";
		} else {
			throw new IllegalStateException("Unsupported platform");
		}

		OmniLog.info("Omnilook mixin plugin detected platform: " + platform);

		Reader raw = new InputStreamReader(MixinPlugin.class.getClassLoader().getResourceAsStream("META-INF/mixinlist.json"));
		Class<?> Gson =
				classExists("org.spongepowered.include.com.google.gson.Gson")
						? Class.forName("org.spongepowered.include.com.google.gson.Gson")
				: classExists("com.google.gson.Gson")
						? Class.forName("com.google.gson.Gson")
				: null;
		if(Gson == null) {
			throw new IllegalStateException("Gson not found");
		}

		Object gson = Gson.getConstructor().newInstance();
		@SuppressWarnings("unchecked")
		Map<String, List<String>> mixins = (Map<String, List<String>>) Gson.getMethod("fromJson", Reader.class, Class.class).invoke(gson, raw, Map.class);
		for(String platform : mixins.keySet()) {
			List<String> list = mixins.get(platform);
			for(int i = 0; i < list.size(); i++) {
				list.set(i, platform + "." + list.get(i));
			}
		}
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

		OmniLog.info("Found mixins: " + m);
		return m;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
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
