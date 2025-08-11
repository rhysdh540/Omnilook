package dev.rdh.omnilook.mixin.rift;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;

// this is extremely scuffed but all the other ways i tried either did nothing or broke the camera spectacularly
@SuppressWarnings("InjectLocalCaptureCanBeReplacedWithLocal")
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Unique
	private float[] omnilook$pitchYaw;

	@SuppressWarnings("DiscouragedShift")
	@Inject(method = "transformCamera", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/Minecraft;getCamera()Lnet/minecraft/entity/Entity;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void modify(float partialTicks, CallbackInfo ci, Entity entity) {
		Omnilook o = Omnilook.getInstance();
		o.update();
		if (o.isEnabled()) {
			omnilook$pitchYaw = new float[]{entity.pitch, entity.yaw};

			entity.pitch = o.getXRot();
			entity.yaw = o.getYRot();

			Minecraft.getInstance().worldRenderer.onViewChanged();
		}
	}

	@Inject(method = "transformCamera", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void reset(float partialTicks, CallbackInfo ci, Entity entity) {
		Omnilook o = Omnilook.getInstance();
		if (o.isEnabled()) {
			entity.pitch = omnilook$pitchYaw[0];
			entity.yaw = omnilook$pitchYaw[1];
		}
	}
}
