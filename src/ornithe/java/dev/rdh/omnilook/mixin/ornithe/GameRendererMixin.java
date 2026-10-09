package dev.rdh.omnilook.mixin.ornithe;

import dev.rdh.omnilook.Omnilook;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	// indev–inf-20100629: m_23117156(F)
	// inf-20100630–1.8.2-pre1: render(F)
	// 1.8.2-pre2–1.12.2: render(FJ)
	// 17w43a–1.14.4: render(FJZ)
	@Inject(method = {"render(F)V", "render(FJ)V", "render(FJZ)V", "m_08357947", "m_23117156"}, remap = false, at = @At("HEAD"))
	private void update(CallbackInfo ci) {
		Omnilook.getInstance().update();
	}

}
