package dev.rdh.omnilook.transform;

import net.lenni0451.classtransform.annotations.CLocalVariable;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTarget.Shift;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.src.ActiveRenderInfo;

@CTransformer(ActiveRenderInfo.class)
public class TransformActiveRenderInfo {
	@SuppressWarnings({"UnusedAssignment", "ParameterCanBeLocal"})
	@CInject(method = "updateRenderInfo", target = @CTarget(value = "GETFIELD", target = "net/minecraft/src/EntityPlayer.rotationYaw:F", shift = Shift.AFTER))
	public static void a(@CLocalVariable(index = 2, modifiable = true) float pitch, @CLocalVariable(index = 3, modifiable = true) float yaw) {
		Omnilook o = Omnilook.getInstance();

		if (o.isEnabled()) {
			pitch = o.getXRot();
			yaw = o.getYRot();
		}
	}
}
