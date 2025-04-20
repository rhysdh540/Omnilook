package dev.rdh.omnilook.mixin.rift;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.MouseHelper;
import net.minecraft.client.entity.EntityPlayerSP;

@Mixin(MouseHelper.class)
public class MouseHelperMixin {
	@Redirect(method = "updatePlayerLook", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;rotateTowards(DD)V"))
	private void update(EntityPlayerSP instance, double yRot, double xRot) {
		if (Omnilook.getInstance().updateCamera((float) xRot, (float) yRot)) {
			instance.rotateTowards(yRot, xRot);
		}
	}
}
