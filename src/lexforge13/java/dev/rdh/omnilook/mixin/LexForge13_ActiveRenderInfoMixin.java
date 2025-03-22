package dev.rdh.omnilook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;

@Mixin(ActiveRenderInfo.class)
public class LexForge13_ActiveRenderInfoMixin {
	@Redirect(method = "updateRenderInfo(Lnet/minecraft/entity/Entity;ZF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationPitch:F"))
	private static float modifyPitch(Entity entity) {
		Omnilook o = Omnilook.getInstance();
		o.updateKey();

		if (o.isEnabled()) {
			return o.getXRot();
		} else {
			return entity.rotationPitch;
		}
	}

	@Redirect(method = "updateRenderInfo(Lnet/minecraft/entity/Entity;ZF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationYaw:F"))
	private static float modifyYaw(Entity entity) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			return o.getYRot();
		} else {
			return entity.rotationYaw;
		}
	}
}
