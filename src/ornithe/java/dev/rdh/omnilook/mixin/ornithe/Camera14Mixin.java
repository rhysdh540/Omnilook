package dev.rdh.omnilook.mixin.ornithe;

import dev.rdh.omnilook.Omnilook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Pseudo
@Mixin(targets = "net.minecraft.unmapped.C_37620400", remap = false)
public class Camera14Mixin {
	// 19w11a+: Camera.update sets its own yaw/pitch instead of reading entity angles
	@ModifyArgs(method = "m_46087421", at = @At(value = "INVOKE", target = "Lnet/minecraft/unmapped/C_37620400;m_26363754(FF)V", ordinal = 0))
	private void rotation(Args args) {
		Omnilook o = Omnilook.getInstance();
		if(o.isEnabled()) {
			args.set(0, o.getYRot());
			args.set(1, o.getXRot());
		}
	}
}
