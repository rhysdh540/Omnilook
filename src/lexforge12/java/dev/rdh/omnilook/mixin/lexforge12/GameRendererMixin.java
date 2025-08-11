package dev.rdh.omnilook.mixin.lexforge12;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.render.GameRenderer;


@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Dynamic
	@WrapWithCondition(method = {
			"render(FJ)V", // 1.8+
			"func_78480_b(F)V" // 1.7.10-
	}, at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;updateLocalPlayerCamera(FF)V"), // 1.8+
			@At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;func_70082_c(FF)V") // 1.7.10-
	})
	private boolean onTurn(@Coerce Object instance, float yaw, float pitch) {
		return Omnilook.getInstance().updateCamera(-pitch, yaw);
	}

	@Dynamic
	@ModifyExpressionValue(method = "transformCamera", at = {
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;yaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevYaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationYaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;prevRotationYaw:F")
	})
	private float modifyYaw(float value) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			return o.getYRot();
		}
		return value;
	}

	@Dynamic
	@ModifyExpressionValue(method = "transformCamera", at = {
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;pitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevPitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationPitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;prevRotationPitch:F")
	})
	private float modifyPitch(float value) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			return o.getXRot();
		}
		return value;
	}
}
