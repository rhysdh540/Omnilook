package dev.rdh.omnilook.mixin.lexforge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.Camera;

// no thanks to lexforge's ComputeCameraAngles being dumb
// and also to ModifyArgs being broken on lexforge too >:(
@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow
	protected abstract void setRotation(float yRot, float xRot);

	@Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
	private void hookRotation(Camera thiz, float yRot, float xRot) {
		Omnilook o = Omnilook.getInstance();
		o.update();

		if (o.isEnabled()) {
			xRot = o.getXRot();
			yRot = o.getYRot();
		}

		this.setRotation(yRot, xRot);
	}
}
