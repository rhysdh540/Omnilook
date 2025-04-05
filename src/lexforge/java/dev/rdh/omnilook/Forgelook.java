package dev.rdh.omnilook;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

@Mod(Omnilook.ID)
public final class Forgelook extends Omnilook {
	public final KeyMapping key;

	public Forgelook() {
		key = new KeyMapping(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KeyMapping.CATEGORY_MISC);

		if(FMLEnvironment.dist != Dist.CLIENT) {
			log.error("Omnilook is a client-side mod and cannot be loaded on a server.");
			return;
		}

		// this is kind of jank but RegisterKeyMappingsEvent doesn't exist until like 1.19
		// and ClientRegistry (the old way to do it) changed packages a lot
		Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, key);
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft.getInstance().options.setCameraType(CameraType.values()[cameraType]);
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getInstance().options.getCameraType().ordinal();
	}

	@Override
	protected float getMCXRot() {
		return Minecraft.getInstance().cameraEntity.getXRot();
	}

	@Override
	protected float getMCYRot() {
		return Minecraft.getInstance().cameraEntity.getYRot();
	}
}
