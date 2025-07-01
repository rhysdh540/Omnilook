package dev.rdh.omnilook;

import org.lwjgl.glfw.GLFW;

import dev.rdh.omnilook.config.Config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;

public final class Forgelook extends Omnilook {
	private final KeyMapping key;

	public Forgelook() {
		key = new KeyMapping(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KEYBINDING_CATEGORY);

		if(FMLEnvironment.dist != Dist.CLIENT) {
			Omnilook.LOGGER.error("Omnilook is a client-side mod and cannot be loaded on a server.");
			return;
		}

		// TODO: figure out how to get the FMLJavaModLoadingContext instance without get()
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterKeyMappings);

		MinecraftForge.registerConfigScreen(parent -> {
			Config.openTextEditor();
			return null;
		});
	}

	void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(key);
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft.getInstance().options.setCameraType(CameraType.values()[cameraType]);
		Minecraft.getInstance().gameRenderer.checkEntityPostEffect(cameraType == 0 ? Minecraft.getInstance().getCameraEntity() : null);
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
