package dev.rdh.omnilook;

import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
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
		throw new NoSuchFieldError("No such field in class " + clazz.getName() + ": " + Arrays.toString(names));
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

		if(classExists("net.neoforged.fml.common.Mod")) {
			platform = "NeoForge";
		} else if(classExists("net.minecraftforge.versions.forge.ForgeVersion")) {
			String forgeVersion = (String) Class.forName("net.minecraftforge.versions.forge.ForgeVersion").getMethod("getVersion").invoke(null);
			int major = Integer.parseInt(forgeVersion.substring(0, forgeVersion.indexOf('.')));
			if(major >= 37) {
				platform = "LexForge";
			} else if(major >= 26) {
				platform = "LexForge16";
			} else if(major == 25) {
				// soon :(
				platform = "LexForge13";
				throw new IllegalStateException("Forge 1.13 not supported yet :(");
			} else {
				throw new IllegalStateException("Unexpected forge version: " + forgeVersion);
			}
		} else if(classExists("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper")) {
			platform = "Fabric";
		} else if(classExists("org.dimdev.rift.Rift")) {
			platform = "Rift";
		} else if(classExists("net.minecraft.command.ICommand")) {
			platform = "LexForge12";
		} else {
			throw new IllegalStateException("Unsupported platform");
		}

		Omnilook.log.info("Omnilook mixin plugin detected platform: " + platform);
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
			case "LexForge16":
				return Arrays.asList(
						"lexforge16.CameraMixin",
						"lexforge16.MouseHandlerMixin"
				);
			case "LexForge12":
				return Arrays.asList(
						"lexforge12.EntityRendererMixin",
						"lexforge12.ActiveRenderInfoMixin"
				);
			case "Fabric":
				return Arrays.asList(
						"fabric.CameraMixin",
						"fabric.MouseHandlerMixin"
				);
			case "Rift":
				return Arrays.asList(
						"rift.MouseHelperMixin",
						"rift.GameRendererMixin",
						"rift.ActiveRenderInfoMixin"
				);
			default:
				throw new IllegalStateException("Mixins not found, what??? Platform: " + platform);
		}
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return mixinClassName.contains(platform.toLowerCase());
	}

	// <editor-fold desc="Unused" defaultstate="collapsed">
	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
	// </editor-fold>
}
