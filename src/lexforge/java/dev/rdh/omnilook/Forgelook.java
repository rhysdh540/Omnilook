package dev.rdh.omnilook;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

@Mod(Omnilook.ID)
public final class Forgelook extends Omnilook {
	public final KeyMapping key;

	@SuppressWarnings("removal")
	public Forgelook() {
		key = new KeyMapping(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KeyMapping.CATEGORY_MISC);

		if(FMLEnvironment.dist != Dist.CLIENT) {
			log.error("Omnilook is a client-side mod and cannot be loaded on a server.");
			return;
		}

		// TODO do this with less try catch
		try {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(
					EventPriority.NORMAL,
					true,
					RegisterKeyMappingsEvent.class,
					this::onRegisterKeyMappings
			);
		} catch (NoClassDefFoundError e) {
			// ClientRegistry does the same thing but has a bunch of different package names so it's easier to just do this
			Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, key);
		}
	}

	void onRegisterKeyMappings(Object event) {
		event.getClass().getDeclaredMethod("register", KeyMapping.class).invoke(event, key);
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
