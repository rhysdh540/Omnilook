package dev.rdh.omnilook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

@Mixin(MouseHandler.class)
public abstract class LexForge_MouseHandlerMixin {
	// TODO there's gotta be a better way to do this
	@Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private void update(LocalPlayer instance, double yRot, double xRot) {
		if (Omnilook.getInstance().update((float) xRot, (float) yRot)) {
			instance.turn(yRot, xRot);
		}
	}
}
