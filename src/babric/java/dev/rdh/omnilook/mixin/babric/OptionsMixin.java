package dev.rdh.omnilook.mixin.babric;

import com.llamalad7.mixinextras.lib.apache.commons.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.Babriclook;
import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;

@Mixin(Options.class)
public class OptionsMixin {
	@Shadow public KeyMapping[] keyMappings;

	@Inject(method = "load", at = @At("HEAD"))
	public void onLoad(CallbackInfo ci) {
		Babriclook b = (Babriclook) Omnilook.getInstance();
		this.keyMappings = ArrayUtils.add(this.keyMappings, b.key);
	}
}
