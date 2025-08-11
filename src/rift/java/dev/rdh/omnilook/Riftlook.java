package dev.rdh.omnilook;

import org.dimdev.rift.listener.client.KeyBindingAdder;
import org.lwjgl.glfw.GLFW;

import dev.rdh.omnilook.config.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.options.KeyBinding;
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
		Minecraft.getInstance().options.perspective = cameraType;

		Minecraft.getInstance().gameRenderer.updateShader(cameraType == 0 ? Minecraft.getInstance().getCamera() : null);

		Minecraft.getInstance().worldRenderer.onViewChanged();
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getInstance().options.perspective;
	}

	@Override
	protected float getMCXRot() {
		return Minecraft.getInstance().getCamera().pitch;
	}

	@Override
	protected float getMCYRot() {
		return Minecraft.getInstance().getCamera().yaw;
	}

	@Override
	protected boolean isKeyClicked() {
		return key.consumeClick();
	}

	@Override
	protected boolean isKeyDown() {
		return key.isPressed();
	}
}
