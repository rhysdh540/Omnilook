package dev.rdh.omnilook;

import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class MixinConfigPlugin implements IMixinConfigPlugin {
	private String platform;

	private static boolean classExists(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@Override
	public void onLoad(String mixinPackage) {
		if(classExists("net.neoforged.fml.common.Mod")) {
			platform = "NeoForge";
		} else if(classExists("net.minecraftforge.versions.forge.ForgeVersion")) {
			String forgeVersion = (String) Class.forName("net.minecraftforge.versions.forge.ForgeVersion").getMethod("getVersion").invoke(null);
			int major = Integer.parseInt(forgeVersion.substring(0, forgeVersion.indexOf('.')));
			if(major >= 37) {
				platform = "LexForge";
			} else {
				throw new IllegalStateException("Forge 1.16- not supported yet");
			}
		} else if (classExists("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper")) {
			platform = "Modern";
		} else {
			throw new IllegalStateException("Unsupported platform");
		}

		Omnilook.log.info("Freelook mixin plugin detected platform: {}", platform);
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return mixinClassName.contains(platform);
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
