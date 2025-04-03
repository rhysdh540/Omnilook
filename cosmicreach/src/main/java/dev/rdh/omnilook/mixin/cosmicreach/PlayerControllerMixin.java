package dev.rdh.omnilook.mixin.cosmicreach;

import com.badlogic.gdx.math.Vector3;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import finalforeach.cosmicreach.entities.PlayerController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.Cosmiclook;
import dev.rdh.omnilook.Omnilook;

@Mixin(PlayerController.class)
public class PlayerControllerMixin {
	@Inject(method = "moveCamera", at = @At(value = "FIELD", target = "Lfinalforeach/cosmicreach/entities/PlayerController;tmpV1:Lcom/badlogic/gdx/math/Vector3;", ordinal = 0))
	private void onMoveCamera(CallbackInfo ci, @Local LocalRef<Vector3> viewDir) {
		Cosmiclook c = (Cosmiclook) Omnilook.getInstance();
		c.update();
		if (c.isEnabled()) {
			viewDir.set(c.viewDir);
		}
	}

	@ModifyExpressionValue(method = "updateCamera", at = @At(value = "FIELD", target = "Lfinalforeach/cosmicreach/entities/Entity;viewDirection:Lcom/badlogic/gdx/math/Vector3;"))
	private Vector3 onUpdateCamera(Vector3 viewDir) {
		Cosmiclook c = (Cosmiclook) Omnilook.getInstance();
		if (c.isEnabled()) {
			return c.viewDir;
		}
		return viewDir;
	}
}
