package dev.rdh.omnilook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.MouseHelper;
import net.minecraft.client.entity.EntityPlayerSP;

@Mixin(MouseHelper.class)
public abstract class LexForge13_MouseHelperMixin {
	@Redirect(method = "updatePlayerLook", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;rotateTowards(DD)V"))
	private void update(EntityPlayerSP instance, double yRot, double xRot) {
		if(Omnilook.getInstance().update((float) xRot, (float) yRot)) {
			instance.rotateTowards(yRot, xRot);
		}
	}
}
