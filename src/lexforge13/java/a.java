import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MouseHelper;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;

@Mixin(ActiveRenderInfo.class)
public class a {
	@Inject(method = "updateRenderInfo(Lnet/minecraft/entity/Entity;ZF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;rotationX:F"))
	private static void hookRotation(Entity p_197924_0_, boolean p_197924_1_, float p_197924_2_, CallbackInfo ci) {
	}
}
