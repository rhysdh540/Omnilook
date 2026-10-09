package dev.rdh.omnilook.mixin.bta;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.render.WorldRenderer;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Inject(method = "updateCameraAndRender", at = @At("HEAD"))
	private void update(float partialTick, CallbackInfo ci) {
		Omnilook.getInstance().update();
	}

	@WrapWithCondition(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/PlayerLocal;interpolateTurn(FF)V"))
	private boolean updateCamera(PlayerLocal player, float yRot, float xRot) {
		return Omnilook.getInstance().updateCamera(-xRot, yRot);
	}
}
