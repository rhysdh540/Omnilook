package dev.rdh.omnilook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import dev.rdh.omnilook.Modernlook;
import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.Camera;

@Mixin(Camera.class)
public abstract class Modern_CameraMixin {
	@ModifyArgs(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
	private void hookRotation(Args args) {
		Modernlook fl = (Modernlook) Omnilook.getInstance();
		fl.updateKey(fl.key.consumeClick(), fl.key.isDown());

		if (fl.isEnabled()) {
			args.setAll(fl.getYRot(), fl.getXRot());
		}
	}
}
