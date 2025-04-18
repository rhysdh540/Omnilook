package dev.rdh.omnilook;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import dev.rdh.omnilook.compat.LexForge20Screens;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;

public final class Forgelook20 extends Omnilook {
	private final KeyMapping key;

	public Forgelook20() {
		key = new KeyMapping(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KEYBINDING_CATEGORY);

		if(FMLEnvironment.dist != Dist.CLIENT) {
			OmniLog.error("Omnilook is a client-side mod and cannot be loaded on a server.");
			return;
		}

		MinecraftForge.registerConfigScreen(LexForge20Screens::make);

		// this is kind of jank but RegisterKeyMappingsEvent doesn't exist until like 1.19
		// and ClientRegistry (the old way to do it) changed packages a lot
		Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, key);
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft.getInstance().options.setCameraType(CameraType.values()[cameraType]);
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getInstance().options.getCameraType().ordinal();
	}

	@Override
	protected float getMCXRot() {
		return Minecraft.getInstance().cameraEntity.getXRot();
	}

	@Override
	protected float getMCYRot() {
		return Minecraft.getInstance().cameraEntity.getYRot();
	}

	@Override
	protected boolean isKeyClicked() {
		return key.consumeClick();
	}

	@Override
	protected boolean isKeyDown() {
		return key.isDown();
	}
}
