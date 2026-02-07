package dev.rdh.omnilook.mixin.legacyfabric;

import com.google.common.collect.Lists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.LegacyFabriclook;
import dev.rdh.omnilook.MixinPlugin;
import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;

import java.util.ArrayList;

@Mixin(GameOptions.class)
public class GameSettingsMixin {
	@Shadow public KeyBinding[] keyBindings;

	@Inject(method = "load", at = @At("HEAD"))
	public void loadKey(CallbackInfo ci) {
		if(MixinPlugin.classExists("net.legacyfabric.fabric.api.client.keybinding.v1.KeyBindingHelper")) {
			return;
		}

		LegacyFabriclook lfl = (LegacyFabriclook) Omnilook.getInstanceOrNull();
		if(lfl != null) {
			ArrayList<KeyBinding> kbs = Lists.newArrayList(this.keyBindings);
			kbs.remove(lfl.key);
			kbs.add(lfl.key);
			this.keyBindings = kbs.toArray(new KeyBinding[0]);
		}
	}
}
