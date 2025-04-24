package dev.rdh.omnilook.mixin.rift;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.Entity;

// this is extremely scuffed but all the other ways i tried either did nothing or broke the camera spectacularly
@SuppressWarnings("InjectLocalCaptureCanBeReplacedWithLocal")
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Unique
	private float[] omnilook$pitchYaw;

	@SuppressWarnings("DiscouragedShift")
	@Inject(method = "orientCamera", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void modify(float partialTicks, CallbackInfo ci, Entity entity) {
		Omnilook o = Omnilook.getInstance();
		o.update();
		if (o.isEnabled()) {
			omnilook$pitchYaw = new float[]{entity.rotationPitch, entity.rotationYaw};

			entity.rotationPitch = o.getXRot();
			entity.rotationYaw = o.getYRot();

			Minecraft.getInstance().worldRenderer.setDisplayListEntitiesDirty();
		}
	}

	@Inject(method = "orientCamera", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void reset(float partialTicks, CallbackInfo ci, Entity entity) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			entity.rotationPitch = omnilook$pitchYaw[0];
			entity.rotationYaw = omnilook$pitchYaw[1];
		}
	}
}
