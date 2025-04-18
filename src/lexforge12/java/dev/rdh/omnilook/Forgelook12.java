package dev.rdh.omnilook;

import org.lwjgl.input.Keyboard;


import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.Launch;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.file.Path;

public class Forgelook12 extends Omnilook {
	private final KeyBinding key;
	private final MethodHandle getRenderViewEntity;

	public Forgelook12() {
		key = new KeyBinding(KEYBINDING_NAME, Keyboard.KEY_GRAVE, KEYBINDING_CATEGORY);
		Field f = MixinPlugin.field(Minecraft.class, "field_175622_Z", "field_71451_h", "renderViewEntity");
		f.setAccessible(true);
		getRenderViewEntity = MethodHandles.lookup().unreflectGetter(f);

		ClientRegistry.registerKeyBinding(key);
	}

	@Override
	public Path getConfigDir() {
		return Launch.minecraftHome.toPath().resolve("config");
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft.getMinecraft().gameSettings.thirdPersonView = cameraType;
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getMinecraft().gameSettings.thirdPersonView;
	}

	@Override
	protected float getMCXRot() {
		return ((Entity) getRenderViewEntity.invoke(Minecraft.getMinecraft())).rotationPitch;
	}

	@Override
	protected float getMCYRot() {
		return ((Entity) getRenderViewEntity.invoke(Minecraft.getMinecraft())).rotationYaw;
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
