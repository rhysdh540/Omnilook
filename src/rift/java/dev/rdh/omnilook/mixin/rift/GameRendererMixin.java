package dev.rdh.omnilook.mixin.rift;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.Entity;

// this is extremely scuffed but all the other ways i tried either did nothing or broke the camera spectacularly
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "orientCamera", at = @At("HEAD"))
	private void a(float partialTicks, CallbackInfo ci, @Share("yaw") LocalFloatRef yaw, @Share("pitch") LocalFloatRef pitch) {
		Omnilook o = Omnilook.getInstance();
		o.update();
		if (o.isEnabled()) {
			Entity entity = Minecraft.getInstance().getRenderViewEntity();
			pitch.set(entity.rotationPitch);
			yaw.set(entity.rotationYaw);

			entity.rotationPitch = o.getXRot();
			entity.rotationYaw = o.getYRot();
		}
	}

	@Inject(method = "orientCamera", at = @At("TAIL"))
	private void b(float partialTicks, CallbackInfo ci, @Share("yaw") LocalFloatRef yaw, @Share("pitch") LocalFloatRef pitch) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			Entity entity = Minecraft.getInstance().getRenderViewEntity();
			entity.rotationPitch = pitch.get();
			entity.rotationYaw = yaw.get();
		}
	}
}
