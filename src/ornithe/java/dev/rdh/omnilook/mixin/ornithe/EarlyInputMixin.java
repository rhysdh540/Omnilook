package dev.rdh.omnilook.mixin.ornithe;

import dev.rdh.omnilook.Omnilook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// indev–inf-20100625-1917 writes angles in the renderer, inf-20100627/29 in Minecraft.run
@Mixin({Minecraft.class, GameRenderer.class})
public class EarlyInputMixin {
	@Redirect(method = {"run", "m_23117156"}, remap = false, require = 0, at = @At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_80130373:F", opcode = Opcodes.PUTFIELD, ordinal = 0))
	private void turnYaw(Entity entity, float value) {
		if(Omnilook.getInstance().updateCamera(0, (value - entity.yaw) / 0.15F)) entity.yaw = value;
	}

	@Redirect(method = {"run", "m_23117156"}, remap = false, require = 0, at = @At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_03549461:F", opcode = Opcodes.PUTFIELD, ordinal = 0))
	private void turnPitch(Entity entity, float value) {
		if(Omnilook.getInstance().updateCamera((value - entity.pitch) / 0.15F, 0)) entity.pitch = value;
	}

}
