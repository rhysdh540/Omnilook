package dev.rdh.omnilook.mixin.fabric;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Dynamic
	@WrapWithCondition(method = {
			"turnPlayer(D)V", // 1.20.5+
			"method_1606()V" // 1.20.4-
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private boolean update(LocalPlayer instance, double yRot, double xRot) {
		return Omnilook.getInstance().updateCamera((float) xRot, (float) yRot);
	}
}
