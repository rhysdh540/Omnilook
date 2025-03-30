package dev.rdh.omnilook;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.src.KeyBinding;

import java.lang.reflect.Field;
import java.nio.file.Path;

public class ModLoaderlook extends Omnilook {
	private final KeyBinding key;
	private final Field mc;

	ModLoaderlook() {
		key = new KeyBinding(KEYBINDING_NAME, Keyboard.KEY_GRAVE);
		this.mc = Minecraft.class.getDeclaredField("theMinecraft");
		mc.setAccessible(true);
	}

	public Minecraft getMinecraft() {
		return (Minecraft) mc.get(null);
	}

	@Override
	public Path getConfigDir() {
		return Launch.minecraftHome.toPath().resolve("config");
	}

	@Override
	protected void setCameraType(int cameraType) {
		getMinecraft().gameSettings.thirdPersonView = cameraType;
	}

	@Override
	protected int getCameraType() {
		return getMinecraft().gameSettings.thirdPersonView;
	}

	@Override
	protected float getMCXRot() {
		return getMinecraft().renderViewEntity.rotationPitch;
	}

	@Override
	protected float getMCYRot() {
		return getMinecraft().renderViewEntity.rotationYaw;
	}

	@Override
	protected boolean isKeyClicked() {
		return key.isPressed();
	}

	@Override
	protected boolean isKeyDown() {
		return key.pressed;
	}
}
