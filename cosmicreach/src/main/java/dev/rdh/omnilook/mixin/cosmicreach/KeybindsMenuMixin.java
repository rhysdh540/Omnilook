package dev.rdh.omnilook.mixin.cosmicreach;

import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.KeybindsMenu;
import finalforeach.cosmicreach.settings.Keybind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.Cosmiclook;
import dev.rdh.omnilook.Omnilook;

@Mixin(KeybindsMenu.class)
public abstract class KeybindsMenuMixin {
	@Shadow
	protected abstract void addKeybindButton(String label, Keybind keybind);

	@Inject(method = "<init>", at = @At("TAIL"))
	private void addButton(GameState previousState, CallbackInfo ci) {
		this.addKeybindButton("Toggle Freelook", ((Cosmiclook) Omnilook.getInstance()).key);
	}
}
