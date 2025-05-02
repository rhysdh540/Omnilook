package dev.rdh.omnilook.mixin.lexforge13;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.rdh.omnilook.Omnilook;

import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

// forge wtf
@Mixin(ModInfo.class)
public class ModInfoMixin {
	@Shadow @Final private String modId;

	@Inject(method = "hasConfigUI", at = @At("HEAD"), cancellable = true)
	private void bruh(CallbackInfoReturnable<Boolean> cir) {
		if (this.modId.equals(Omnilook.ID)) {
			cir.setReturnValue(true);
		}
	}
}
