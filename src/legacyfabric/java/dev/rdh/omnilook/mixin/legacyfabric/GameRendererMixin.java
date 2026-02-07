package dev.rdh.omnilook.mixin.legacyfabric;

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
			"method_1331(F)V" // 1.7.10-
	}, at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;updateLocalPlayerCamera(FF)V"), // 1.8+
			@At(value = "INVOKE", target = "Lnet/minecraft/class_481;method_2534(FF)V") // 1.7.10-
	})
	private boolean onTurn(@Coerce Object instance, float yaw, float pitch) {
		return Omnilook.getInstance().updateCamera(-pitch, yaw);
	}

	@Dynamic
	@ModifyExpressionValue(method = "transformCamera", at = {
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;yaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevYaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/class_1699;field_3258:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/class_1699;field_3194:F")
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
			@At(value = "FIELD", target = "Lnet/minecraft/class_1699;field_3193:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/class_1699;field_3195:F")
	})
	private float modifyPitch(float value) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			return o.getXRot();
		}
		return value;
	}
}
