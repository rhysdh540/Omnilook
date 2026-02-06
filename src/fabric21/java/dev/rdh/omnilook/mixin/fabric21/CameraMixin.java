package dev.rdh.omnilook.mixin.fabric21;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.Camera;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Dynamic
	@ModifyArgs(method = {
			"setup",
			"method_19321(Lnet/minecraft/class_1922;Lnet/minecraft/class_1297;ZZF)V" // uses BlockGetter instead of Level before 21.11?
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"), require = 1)
	private void hookRotation(Args args) {
		Omnilook o = Omnilook.getInstance();
		o.update();

		if (o.isEnabled()) {
			args.setAll(o.getYRot(), o.getXRot());
		}
	}
}
