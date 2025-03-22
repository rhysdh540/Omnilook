package dev.rdh.omnilook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.rdh.omnilook.Forgelook16;
import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.Camera;

// no thanks to lexforge's ComputeCameraAngles being dumb
// and also to ModifyArgs being broken on lexforge too >:(
@Mixin(Camera.class)
public abstract class LexForge16_CameraMixin {

	@Shadow
	protected abstract void setRotation(float yRot, float xRot);

	@Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
	private void hookRotation(Camera thiz, float yRot, float xRot) {
		Forgelook16 fl = (Forgelook16) Omnilook.getInstance();
		fl.updateKey(fl.key.consumeClick(), fl.key.isDown());

		if (fl.isEnabled()) {
			xRot = fl.getXRot();
			yRot = fl.getYRot();
		}

		this.setRotation(yRot, xRot);
	}
}
