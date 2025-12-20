package dev.rdh.omnilook;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

import dev.rdh.omnilook.config.ModMenuScreenProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;

import java.nio.file.Path;

public class Avianlook extends Omnilook implements ModMenuScreenProvider<Screen> {
	private final KeyBinding key;

	public Avianlook() {
		KeyBinding key;
		try {
			key = new KeyBinding(KEYBINDING_NAME, Keyboard.KEY_GRAVE, KEYBINDING_CATEGORY);
		} catch (NoSuchMethodError e) {
			key = KeyBinding.class.getDeclaredConstructor(String.class, int.class).newInstance(KEYBINDING_NAME, Keyboard.KEY_GRAVE);
		}
		this.key = key;

		Minecraft.getInstance().options.keyBindings = ArrayUtils.add(Minecraft.getInstance().options.keyBindings, key);
	}

	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
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
