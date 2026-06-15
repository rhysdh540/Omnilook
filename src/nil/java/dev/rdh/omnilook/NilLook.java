package dev.rdh.omnilook;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.options.KeyBinding;

import java.nio.file.Path;

public class NilLook extends Omnilook {
	public final KeyBinding key;

	public NilLook() {
		key = new KeyBinding(KEYBINDING_NAME, Keyboard.KEY_GRAVE);
	}

	@Override
	public Path getConfigDir() {
		return Minecraft.getInstance().runDir.toPath().resolve("config");
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft.getInstance().options.perspective = cameraType;
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getInstance().options.perspective;
	}

	@Override
	protected float getMCXRot() {
		return Minecraft.getInstance().camera.pitch;
	}

	@Override
	protected float getMCYRot() {
		return Minecraft.getInstance().camera.yaw;
	}

	@Override
	protected boolean isKeyClicked() {
		return key.consumeClick();
	}

	@Override
	protected boolean isKeyDown() {
		return key.pressed;
	}
}
