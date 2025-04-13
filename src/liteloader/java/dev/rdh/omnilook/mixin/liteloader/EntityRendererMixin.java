package dev.rdh.omnilook.mixin.liteloader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;

@Mixin(value = EntityRenderer.class)
public class EntityRendererMixin {
	@Redirect(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V"))
	private void onTurn(EntityPlayerSP player, float yaw, float pitch) {
		if (Omnilook.getInstance().isEnabled()) {
			Omnilook.getInstance().updateCamera(-pitch, yaw);
		} else {
			player.turn(yaw, pitch);
		}
	}

	@Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationYaw:F"))
	private float modifyYaw(Entity entity) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			return o.getYRot();
		}
		return entity.rotationYaw;
	}

	@Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationPitch:F"))
	private float modifyPitch(Entity entity) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			return o.getXRot();
		}
		return entity.rotationPitch;
	}

	@Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationYaw:F"))
	private float modifyPrevYaw(Entity entity) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			return o.getYRot();
		}
		return entity.prevRotationYaw;
	}

	@Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationPitch:F"))
	private float modifyPrevPitch(Entity entity) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			return o.getXRot();
		}
		return entity.prevRotationPitch;
	}
}
