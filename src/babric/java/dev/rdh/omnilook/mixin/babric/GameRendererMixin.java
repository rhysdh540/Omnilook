package dev.rdh.omnilook.mixin.babric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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
	@Shadow private float xRot;

	@WrapWithCondition(method = "render(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(FF)V"))
	private boolean onRender(LocalPlayer instance, float yRot, float xRot) {
		Omnilook o = Omnilook.getInstance();
		o.update();
		return o.updateCamera(-xRot, yRot);
	}

	@ModifyExpressionValue(method = "moveCameraToPlayer", at = {
			@At(value = "FIELD", target = "Lnet/minecraft/world/entity/Mob;xRot:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/world/entity/Mob;xRotO:F")
	})
	private float onMoveCameraToPlayer(float original) {
		Omnilook o = Omnilook.getInstance();

		if(o.isEnabled()) {
			return o.getXRot();
		}

		return original;
	}

	@ModifyExpressionValue(method = "moveCameraToPlayer", at = {
			@At(value = "FIELD", target = "Lnet/minecraft/world/entity/Mob;yRot:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/world/entity/Mob;yRotO:F")
	})
	private float onMoveCameraToPlayer2(float original) {
		Omnilook o = Omnilook.getInstance();

		if(o.isEnabled()) {
			return o.getYRot();
		}

		return original;
	}
}
