package dev.rdh.omnilook.mixin.lexforge16;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Group(name = "turn", min = 1, max = 1)
	@Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private void update114(LocalPlayer instance, double yRot, double xRot) {
		if (Omnilook.getInstance().updateCamera((float) xRot, (float) yRot)) {
			instance.turn(yRot, xRot);
		}
	}

	@Group(name = "turn")
	@Dynamic("multiversion")
	@Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;func_195049_a(DD)V"))
	private void update113(@Coerce Entity instance, double yRot, double xRot) {
		if (Omnilook.getInstance().updateCamera((float) xRot, (float) yRot)) {
			instance.turn(yRot, xRot);
		}
	}
}
