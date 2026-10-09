package dev.rdh.omnilook.mixin.bta;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.render.camera.EntityCamera;

@Mixin(EntityCamera.class)
public class EntityCameraMixin {
	@ModifyReturnValue(method = "getXRot(F)D", at = @At("RETURN"))
	private double getXRot(double original) {
		Omnilook o = Omnilook.getInstance();
		return o.isEnabled() ? o.getXRot() : original;
	}

	@ModifyReturnValue(method = "getYRot(F)D", at = @At("RETURN"))
	private double getYRot(double original) {
		Omnilook o = Omnilook.getInstance();
		return o.isEnabled() ? o.getYRot() : original;
	}
}
