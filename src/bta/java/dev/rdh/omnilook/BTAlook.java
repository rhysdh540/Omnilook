package dev.rdh.omnilook;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.input.Keyboard;

import dev.rdh.omnilook.config.ModMenuScreenProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.core.lang.Language;

import java.nio.file.Path;

public class BTAlook extends Omnilook implements ModMenuScreenProvider<Screen> {
	private final KeyBinding key = GameSettings.register(new KeyBinding(KEYBINDING_NAME)
			.setDefault(InputDevice.keyboard, Keyboard.KEY_GRAVE));
	private boolean keyWasPressed;
	private boolean keyPressed;

	public BTAlook() {
		Language.Default.INSTANCE.getEntries().putIfAbsent(KEYBINDING_NAME, "Toggle Freelook");
	}

	@Override
	public void update() {
		boolean pressed = isKeyDown();
		keyPressed = pressed && !keyWasPressed;
		keyWasPressed = pressed;
		if(Minecraft.getMinecraft().thePlayer != null) super.update();
	}

	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	protected void setCameraType(int cameraType) {
		GameSettings.THIRD_PERSON_VIEW.set(cameraType);
	}

	@Override
	protected int getCameraType() {
		return GameSettings.THIRD_PERSON_VIEW.value;
	}

	@Override
	protected float getMCXRot() {
		return Minecraft.getMinecraft().thePlayer.xRot;
	}

	@Override
	protected float getMCYRot() {
		return Minecraft.getMinecraft().thePlayer.yRot;
	}

	@Override
	protected boolean isKeyClicked() {
		return keyPressed;
	}

	@Override
	protected boolean isKeyDown() {
		Minecraft mc = Minecraft.getMinecraft();
		return mc.inGameHasFocus && mc.currentScreen == null && key.isPressed();
	}
}
