package dev.rdh.omnilook.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.Camera;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@ModifyArgs(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
	private void hookRotation(Args args) {
		Omnilook o = Omnilook.getInstance();
		o.update();

		if (o.isEnabled()) {
			args.setAll(o.getYRot(), o.getXRot());
		}
	}
}
