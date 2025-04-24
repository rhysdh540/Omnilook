package dev.rdh.omnilook.mixin.liteloader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.EntityPlayer;

@Mixin(ActiveRenderInfo.class)
public class ActiveRenderInfoMixin {
	@Redirect(method = "updateRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;rotationPitch:F"))
	private static float modifyPitch(EntityPlayer player) {
		Omnilook o = Omnilook.getInstance();
		o.update();
		if(o.isEnabled()) {
			Minecraft.getMinecraft().renderGlobal.setDisplayListEntitiesDirty();
			return o.getXRot();
		}
		return player.rotationPitch;
	}

	@Redirect(method = "updateRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;rotationYaw:F"))
	private static float modifyYaw(EntityPlayer player) {
		Omnilook o = Omnilook.getInstance();
		if(o.isEnabled()) {
			return o.getYRot();
		}
		return player.rotationYaw;
	}
}
