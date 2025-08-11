package dev.rdh.omnilook.mixin.rift;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;updateLocalPlayerCamera(DD)V"))
	private void update(LocalClientPlayerEntity instance, double yRot, double xRot) {
		if (Omnilook.getInstance().updateCamera((float) xRot, (float) yRot)) {
			instance.updateLocalPlayerCamera(yRot, xRot);
		}
	}
}
