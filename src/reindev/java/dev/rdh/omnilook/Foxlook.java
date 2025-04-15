package dev.rdh.omnilook;

import com.fox2code.foxloader.loader.ModLoader;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.src.client.KeyBinding;

import java.nio.file.Path;

public class Foxlook extends Omnilook {
	final KeyBinding key;

	private boolean keyWasPressed = false;
	private boolean keyPressed = false;

	public Foxlook() {
		this.key = new KeyBinding(KEYBINDING_NAME, Keyboard.KEY_GRAVE);
	}

	@Override
	public void update() {
		if(isKeyDown()) {
			keyPressed = !keyWasPressed;
			keyWasPressed = true;
		} else {
			keyPressed = keyWasPressed = false;
		}

		super.update();
	}

	@Override
	public Path getConfigDir() {
		return ModLoader.config.toPath();
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft.getInstance().gameSettings.thirdPersonView = cameraType;
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getInstance().gameSettings.thirdPersonView;
	}

	@Override
	protected float getMCXRot() {
		return Minecraft.getInstance().renderViewEntity.rotationPitch;
	}

	@Override
	protected float getMCYRot() {
		return Minecraft.getInstance().renderViewEntity.rotationYaw;
	}

	@Override
	protected boolean isKeyClicked() {
		return keyPressed;
	}

	@Override
	protected boolean isKeyDown() {
		return Keyboard.isKeyDown(key.keyCode);
	}
}
