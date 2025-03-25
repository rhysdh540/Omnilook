package dev.rdh.omnilook.mixin.lexforge12;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import org.objectweb.asm.*;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;

@Mixin(ActiveRenderInfo.class)
public class ActiveRenderInfoMixin {
	// 1.12-
	@Dynamic
	@Inject(method = {
			"updateRenderInfo(Lnet/minecraft/entity/Entity;Z)V", // 1.7
			"updateRenderInfo(Lnet/minecraft/entity/player/EntityPlayer;Z)V", // 1.8+
			"updateRenderInfo(Lnet/minecraft/entity/Entity;ZF)V" // 1.13
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;rotationX:F", ordinal = 0))
	private static void hookRotation(Entity entity, boolean thirdPerson, float partialTicks, CallbackInfo ci,
										@Local(ordinal = 2) LocalFloatRef pitch, @Local(ordinal = 3) LocalFloatRef yaw) {
		Omnilook o = Omnilook.getInstance();
		o.update();

		if(o.isEnabled()) {
			pitch.set(o.getXRot());
			yaw.set(o.getYRot());
		}
	}
}
