package dev.rdh.omnilook.mixin.foxloader;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.src.client.player.EntityPlayerSP;
import net.minecraft.src.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

	@WrapWithCondition(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/client/player/EntityPlayerSP;func_346_d(FF)V"))
	private boolean onRender(EntityPlayerSP instance, float yRot, float xRot) {
		Omnilook o = Omnilook.getInstance();
		o.update();
		return o.updateCamera(-xRot, yRot);
	}

	@ModifyExpressionValue(method = "orientCamera", at = {
			@At(value = "FIELD", target = "Lnet/minecraft/src/game/entity/EntityLiving;rotationPitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/src/game/entity/EntityLiving;prevRotationPitch:F")
	})
	private float modifyPitch(float original) {
		Omnilook o = Omnilook.getInstance();

		if(o.isEnabled()) {
			return o.getXRot();
		}

		return original;
	}

	@ModifyExpressionValue(method = "orientCamera", at = {
			@At(value = "FIELD", target = "Lnet/minecraft/src/game/entity/EntityLiving;rotationYaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/src/game/entity/EntityLiving;prevRotationYaw:F")
	})
	private float modifyYaw(float original) {
		Omnilook o = Omnilook.getInstance();

		if(o.isEnabled()) {
			return o.getYRot();
		}

		return original;
	}
}
