package dev.rdh.omnilook;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.nio.file.Path;

public class Babriclook extends Omnilook {
	public final KeyMapping key;
	public boolean keyWasDown = false;
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
			if(!keyWasDown) {
				keyWasDown = true;
			}
		} else {
			keyWasDown = false;
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
		return !keyWasDown && Keyboard.isKeyDown(key.key);
	}

	@Override
	protected boolean isKeyDown() {
		return Keyboard.isKeyDown(key.key);
	}
}
