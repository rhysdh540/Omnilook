package dev.rdh.omnilook;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.input.Keyboard;

import dev.rdh.omnilook.config.ModMenuScreenProvider;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.nio.file.Path;

public class Babriclook extends Omnilook implements ModMenuScreenProvider<Screen> {
	public final KeyMapping key;
	private boolean keyWasPressed = false;
	private boolean keyPressed = false;
	public final Minecraft mc;

	public Babriclook() {
		this.key = new KeyMapping(KEYBINDING_NAME, Keyboard.KEY_GRAVE);
		Field f = MixinPlugin.field(Minecraft.class, "instance", "field_2791");
		f.setAccessible(true);
		this.mc = (Minecraft) f.get(null);
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
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	protected void setCameraType(int cameraType) {
		mc.options.thirdPersonView = (cameraType != 0);
	}

	@Override
	protected int getCameraType() {
		return mc.options.thirdPersonView ? 1 : 0;
	}

	@Override
	protected float getMCXRot() {
		return mc.cameraEntity.xRot;
	}

	@Override
	protected float getMCYRot() {
		return mc.cameraEntity.yRot;
	}

	@Override
	protected boolean isKeyClicked() {
		return keyPressed;
	}

	@Override
	protected boolean isKeyDown() {
		return Keyboard.isKeyDown(key.key);
	}
}
