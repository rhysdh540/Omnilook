package dev.rdh.omnilook.mixin.lexforge12;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.Forgelook12;
import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;

import java.lang.invoke.MethodHandle;

@Mixin(ActiveRenderInfo.class)
public class ActiveRenderInfoMixin {
	@Inject(method = {
			"updateRenderInfo(Lnet/minecraft/entity/Entity;Z)V",
			"updateRenderInfo(Lnet/minecraft/entity/player/EntityPlayer;Z)V",
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;rotationX:F", ordinal = 0))
	private static void hookRotation(@Coerce Entity entity, boolean thirdPerson, CallbackInfo ci,
									 @Local(ordinal = 2) LocalFloatRef pitch, @Local(ordinal = 3) LocalFloatRef yaw) {
		Omnilook o = Omnilook.getInstance();
		o.update();

		if(o.isEnabled()) {
			pitch.set(o.getXRot());
			yaw.set(o.getYRot());

			setDisplayListEntitiesDirty.invokeExact();
		}
	}

	@Unique
	private static final MethodHandle setDisplayListEntitiesDirty = Forgelook12.getMH_setDisplayListEntitiesDirty();
}
