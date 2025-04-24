package dev.rdh.omnilook.mixin.legacyfabric;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.LegacyFabriclook;
import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.EntityPlayer;

import java.lang.invoke.MethodHandle;

@Mixin(ActiveRenderInfo.class)
public class ActiveRenderInfoMixin {
	@Inject(method = "updateRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;rotationX:F"))
	private static void hookRotation(EntityPlayer entity, boolean thirdPerson, CallbackInfo ci,
									 @Local(ordinal = 2) LocalFloatRef pitch, @Local(ordinal = 3) LocalFloatRef yaw) {
		Omnilook o = Omnilook.getInstance();
		o.update();

		if (o.isEnabled()) {
			pitch.set(o.getXRot());
			yaw.set(o.getYRot());

			setDisplayListEntitiesDirty.invokeExact();
		}
	}

	@Unique
	private static final MethodHandle setDisplayListEntitiesDirty = LegacyFabriclook.getMH_setDisplayListEntitiesDirty();
}
