package dev.rdh.omnilook.mixin.ornithe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.rdh.omnilook.Omnilook;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(Camera.class)
public class CameraMixin {
	// 1.0.0-rc2-1633–19w09a: static camera yaw/pitch used by particles/billboards
	@ModifyExpressionValue(method = {"setup", "m_15661785"}, remap = false, at = {
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_40996800;f_80130373:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/player/PlayerEntity;yaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_80130373:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/LivingEntity;yaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_74900774:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/LivingEntity;lastYaw:F")
	})
	private static float yaw(float value) {
		Omnilook o = Omnilook.getInstance();
		return o.isEnabled() ? o.getYRot() : value;
	}

	@ModifyExpressionValue(method = {"setup", "m_15661785"}, remap = false, at = {
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_40996800;f_03549461:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/player/PlayerEntity;pitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_03549461:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/LivingEntity;pitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_45219594:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/LivingEntity;lastPitch:F")
	})
	private static float pitch(float value) {
		Omnilook o = Omnilook.getInstance();
		return o.isEnabled() ? o.getXRot() : value;
	}
}
