package dev.rdh.omnilook.mixin.ornithe;

import dev.rdh.omnilook.Avianlook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
	@Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V", remap = false, require = 0, at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/unmapped/C_76792751;m_70449379()V"),
			@At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;load()V")
	})
	private void initialize(Minecraft mc, File directory, CallbackInfo ci) {
		Avianlook.initialize(mc, (GameOptions) (Object) this);
	}
}
