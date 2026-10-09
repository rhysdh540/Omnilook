package dev.rdh.omnilook;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public abstract class BaseMojmapImpl extends Omnilook {
	public final KeyMapping key;

	protected BaseMojmapImpl() {
		int grave = InputConstants.getKey("key.keyboard.grave.accent").getValue();
		KeyMapping k;
		try {
			k = new KeyMapping(KEYBINDING_NAME, grave, KeyMapping.Category.MISC);
		} catch (Throwable t) {
			//noinspection JavaReflectionMemberAccess
			k = KeyMapping.class.getDeclaredConstructor(String.class, int.class, String.class)
					.newInstance(KEYBINDING_NAME, grave, KEYBINDING_CATEGORY);
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
