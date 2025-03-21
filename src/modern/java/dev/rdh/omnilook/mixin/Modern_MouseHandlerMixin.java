package dev.rdh.omnilook.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

@Mixin(MouseHandler.class)
public abstract class Modern_MouseHandlerMixin {
	@SuppressWarnings("UnresolvedMixinReference")
	@WrapWithCondition(method = {
			"turnPlayer(D)V", // 1.20.5+
			"method_1606()V" // 1.20.4-
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private boolean update(LocalPlayer instance, double yRot, double xRot) {
		return Omnilook.getInstance().update((float) xRot, (float) yRot);
	}
}
