package dev.rdh.omnilook.mixin.ornithe;

import dev.rdh.omnilook.Omnilook;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
	// inf-20100630-1340–1.12.2
	@Inject(method = "updateLocalPlayerCamera(FF)V", at = @At("HEAD"), cancellable = true, require = 0)
	private void turn(float yaw, float pitch, CallbackInfo ci) {
		if(!Omnilook.getInstance().updateCamera(-pitch, yaw)) ci.cancel();
	}

	// 17w43a+
	@Inject(method = {"updateLocalPlayerCamera(DD)V", "m_74052865(DD)V"}, remap = false, at = @At("HEAD"), cancellable = true, require = 0)
	private void turn(double yaw, double pitch, CallbackInfo ci) {
		turn((float) yaw, -(float) pitch, ci);
	}
}
