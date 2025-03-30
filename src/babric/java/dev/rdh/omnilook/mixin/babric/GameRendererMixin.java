package dev.rdh.omnilook.mixin.babric;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@WrapWithCondition(method = "render(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(FF)V"))
	private boolean onRender(LocalPlayer instance, float xRot, float yRot) {
		return Omnilook.getInstance().updateCamera(xRot, yRot);
	}

	@Inject(method = "moveCameraToPlayer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V", ordinal = 1))
	private void onMoveCameraToPlayer(float partialTicks, CallbackInfo ci,
									  @Local(ordinal = 1) LocalFloatRef yRot, @Local(ordinal = 2) LocalFloatRef xRot) {
		Omnilook o = Omnilook.getInstance();
		o.update();

		if(o.isEnabled()) {
			yRot.set(o.getYRot());
			xRot.set(o.getXRot());
		}
	}
}
