package dev.rdh.omnilook;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public abstract class BaseMojmapImpl extends Omnilook {
	protected final KeyMapping key;

	protected BaseMojmapImpl() {
		KeyMapping k;
		try {
			k = new KeyMapping(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KeyMapping.Category.MISC);
		} catch (Throwable t) {
			//noinspection JavaReflectionMemberAccess
			k = KeyMapping.class.getDeclaredConstructor(String.class, int.class, String.class)
					.newInstance(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KEYBINDING_CATEGORY);
		}
		key = k;
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft.getInstance().options.setCameraType(CameraType.values()[cameraType]);
		Minecraft.getInstance().gameRenderer.checkEntityPostEffect(cameraType == 0 ? Minecraft.getInstance().getCameraEntity() : null);
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getInstance().options.getCameraType().ordinal();
	}

	@Override
	protected float getMCXRot() {
		return Minecraft.getInstance().getCameraEntity().getXRot();
	}

	@Override
	protected float getMCYRot() {
		return Minecraft.getInstance().getCameraEntity().getYRot();
	}

	@Override
	protected boolean isKeyClicked() {
		return key.consumeClick();
	}

	@Override
	protected boolean isKeyDown() {
		return key.isDown();
	}
}
