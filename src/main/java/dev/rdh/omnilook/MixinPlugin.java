package dev.rdh.omnilook;

import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class MixinPlugin implements IMixinConfigPlugin {
	private static String platform;

	public static boolean classExists(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

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
			} else {
				throw new IllegalStateException("Forge 1.14- not supported yet");
			}
		} else if (classExists("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper")) {
			platform = "Modern";
		} else {
			throw new IllegalStateException("Unsupported platform");
		}

		Omnilook.log.info("Freelook mixin plugin detected platform: {}", platform);
	}

	// this is really jank but it's the best way I can figure out how to get only the mixins on the classpath to load in dev
	@Override
	@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
	public List<String> getMixins() {
		switch(platform) {
			case "NeoForge":
				return Arrays.asList(
						"NeoForge_MouseHandlerMixin"
				);
			case "LexForge":
				return Arrays.asList(
						"LexForge_CameraMixin",
						"LexForge_MouseHandlerMixin"
				);
			case "LexForge16":
				return Arrays.asList(
						"LexForge16_CameraMixin",
						"LexForge16_MouseHandlerMixin"
				);
			case "Modern":
				return Arrays.asList(
						"Modern_CameraMixin",
						"Modern_MouseHandlerMixin"
				);
			default:
				throw new IllegalStateException("Mixins not found, what??? Platform: " + platform);
		}
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return mixinClassName.contains(platform);
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
