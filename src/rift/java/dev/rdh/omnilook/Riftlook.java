package dev.rdh.omnilook;

import org.dimdev.rift.listener.client.KeyBindingAdder;
import org.lwjgl.glfw.GLFW;

import dev.rdh.omnilook.config.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.launchwrapper.Launch;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

public class Riftlook extends Omnilook implements KeyBindingAdder {
	private final KeyBinding key;

	public Riftlook() {
		key = new KeyBinding(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KEYBINDING_CATEGORY);

		if(MixinPlugin.classExists("me.shedaniel.api.ConfigRegistry")) {
			me.shedaniel.api.ConfigRegistry.registerConfig(Omnilook.ID, Config::openTextEditor);
		}
	}

	@Override
	public Collection<? extends KeyBinding> getKeyBindings() {
		return Collections.singleton(key);
	}

	@Override
	public Path getConfigDir() {
		return Launch.minecraftHome.toPath().resolve("config");
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft.getInstance().gameSettings.thirdPersonView = cameraType;

		Minecraft.getInstance().gameRenderer.loadEntityShader(cameraType == 0 ? Minecraft.getInstance().getRenderViewEntity() : null);

		Minecraft.getInstance().worldRenderer.setDisplayListEntitiesDirty();
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getInstance().gameSettings.thirdPersonView;
	}

	@Override
	protected float getMCXRot() {
		return Minecraft.getInstance().getRenderViewEntity().rotationPitch;
	}

	@Override
	protected float getMCYRot() {
		return Minecraft.getInstance().getRenderViewEntity().rotationYaw;
	}

	@Override
	protected boolean isKeyClicked() {
		return key.isPressed();
	}

	@Override
	protected boolean isKeyDown() {
		return key.isKeyDown();
	}
}
