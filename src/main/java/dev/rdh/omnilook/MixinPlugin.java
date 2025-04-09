package dev.rdh.omnilook;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraftforge.versions.forge.ForgeVersion;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class MixinPlugin implements IMixinConfigPlugin {
	private static String platform;

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
		return platform;
	}

	@Override
	public void onLoad(String mixinPackage) {
		if(platform != null) {
			throw new IllegalStateException("onLoad called twice");
		}

		if(classExists("org.apache.logging.log4j.Logger")) {
			org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("Omnilook");
			OmniLog.init("log4j", logger::info, logger::warn, logger::error, logger::error);
		} else if(classExists("org.slf4j.Logger")) {
			org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("Omnilook");
			OmniLog.init("slf4j", logger::info, logger::warn, logger::error, logger::error);
		} else {
			// there's probably a better way of doing this but it works for now
			OmniLog.init(
					"System.out.println",
					s -> System.out.println("[Omnilook/INFO] " + s),
					s -> System.out.println("[Omnilook/WARN] " + s),
					s -> System.err.println("[Omnilook/ERROR] " + s),
					(s, e) -> {
						System.err.println("[Omnilook/ERROR] " + s);
						e.printStackTrace();
					}
			);
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
	}

	// this is really jank but it's the best way I can figure out how to get only the mixins on the classpath to load in dev
	@Override
	@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
	public List<String> getMixins() {
		switch(platform) {
			case "NeoForge":
				return Arrays.asList(
						"neoforge.MouseHandlerMixin"
				);
			case "LexForge":
				return Arrays.asList(
						"lexforge.CameraMixin",
						"lexforge.MouseHandlerMixin"
				);
			case "LexForge20":
				return Arrays.asList(
						"lexforge20.CameraMixin",
						"lexforge20.MouseHandlerMixin"
				);
			case "LexForge16":
				return Arrays.asList(
						"lexforge16.CameraMixin",
						"lexforge16.MouseHandlerMixin"
				);
			case "LexForge13":
				return Arrays.asList(
						"lexforge16.MouseHandlerMixin",
						"lexforge13.GameRendererMixin"
				);
			case "LexForge12":
				return Arrays.asList(
						"lexforge12.EntityRendererMixin",
						"lexforge12.ActiveRenderInfoMixin"
				);
			case "Fabric":
				return Arrays.asList(
						"fabric.CameraMixin",
						"fabric.MouseHandlerMixin",
						"fabric.OptionsMixin"
				);
			case "LegacyFabric":
				return Arrays.asList(
						"legacyfabric.EntityRendererMixin",
						"legacyfabric.ActiveRenderInfoMixin",
						"legacyfabric.GameSettingsMixin"
				);
			case "Babric":
				return Arrays.asList(
						"babric.GameRendererMixin",
						"babric.OptionsMixin"
				);
			case "Rift":
				return Arrays.asList(
						"rift.MouseHelperMixin",
						"rift.GameRendererMixin"
				);
			case "LiteLoader":
				return Arrays.asList(
						"liteloader.EntityRendererMixin",
						"liteloader.ActiveRenderInfoMixin"
				);
			case "FoxLoader":
				return Arrays.asList(
						"foxloader.EntityRendererMixin"
				);
			default:
				throw new IllegalStateException("Mixins not found, what??? Platform: " + platform);
		}
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return mixinClassName.contains(platform.toLowerCase());
	}

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
	public void preApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	public void postApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
	//endregion
}
