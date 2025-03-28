package dev.rdh.omnilook.mixin.legacyfabric;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.LegacyFabriclook;
import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

@Mixin(GameSettings.class)
public class GameSettingsMixin {
	@Shadow public KeyBinding[] keyBindings;

	@Inject(method = "loadOptions", at = @At("HEAD"))
	public void loadOptions(CallbackInfo ci) {
		this.keyBindings = ArrayUtils.add(this.keyBindings, ((LegacyFabriclook) Omnilook.getInstance()).key);
	}
}
