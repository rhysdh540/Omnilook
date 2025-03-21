package dev.rdh.omnilook;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

@Mod(value = Omnilook.ID, dist = Dist.CLIENT)
public final class Neolook extends Omnilook {
	private final KeyMapping key;

	public Neolook(IEventBus bus) {
		key = new KeyMapping(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KeyMapping.CATEGORY_MISC);
		NeoForge.EVENT_BUS.addListener(this::onComputeCameraAngles);
		bus.addListener(this::onRegisterKeyMappings);
	}

	void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(key);
	}

	void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
		if (key.consumeClick()) {
			this.toggle();
		}

		if (this.isEnabled()) {
			event.setPitch(getXRot());
			event.setYaw(getYRot());
		}
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
