package dev.rdh.omnilook.mixin.lexforge13;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import dev.rdh.omnilook.Omnilook;
import dev.rdh.omnilook.config.Config;

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.gui.GuiModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

// possibly the worst mixin ive ever written
@Mixin(GuiModList.class)
public class GuiModListMixin {
	// some mapping changed sometime so this has to be replaced
	// doing it here because this is the only 1.13-specific class
	static {
		ModList.get().getModContainerById(Omnilook.ID).get().registerExtensionPoint(
				ExtensionPoint.CONFIGGUIFACTORY,
				() -> (mc, parent) -> {
					Config.openTextEditor();
					return parent;
				}
		);
	}

	@Shadow private ModInfo selectedMod;

	// froge hardcodes false so we have to override it to true
	// can't do it in ModInfo because it's loaded before mixins are seemingly
	@ModifyExpressionValue(method = "updateCache", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/loading/moddiscovery/ModInfo;hasConfigUI()Z"))
	private boolean bruh(boolean original) {
		if(this.selectedMod.getModId().equals(Omnilook.ID)) {
			return true;
		}
		return original;
	}
}
