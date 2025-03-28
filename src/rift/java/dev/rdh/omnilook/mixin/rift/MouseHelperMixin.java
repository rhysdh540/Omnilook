package dev.rdh.omnilook.mixin.rift;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.MouseHelper;
import net.minecraft.client.entity.EntityPlayerSP;

@Mixin(MouseHelper.class)
public class MouseHelperMixin {
	@WrapWithCondition(method = "updatePlayerLook", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;rotateTowards(DD)V"))
	private boolean update(EntityPlayerSP instance, double yRot, double xRot) {
		return Omnilook.getInstance().updateCamera((float) xRot, (float) yRot);
	}
}
