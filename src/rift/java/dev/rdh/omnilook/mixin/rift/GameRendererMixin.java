package dev.rdh.omnilook.mixin.rift;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GlStateManager;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@ModifyExpressionValue(method = "orientCamera", at = {
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationYaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationYaw:F"),
	})
	private float modifyYaw(float value) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			GlStateManager.rotatef(o.getYRot(), 0, -1, 0);
			return o.getYRot();
		}
		return value;
	}

	@ModifyExpressionValue(method = "orientCamera", at = {
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationPitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationPitch:F"),
	})
	private float modifyPitch(float value) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			GlStateManager.rotatef(o.getXRot(), -1, 0, 0);
			return o.getXRot();
		}
		return value;
	}
}
