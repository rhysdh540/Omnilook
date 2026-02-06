package dev.rdh.omnilook.mixin.intermediaryfabric;

import com.google.common.collect.Lists;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rdh.omnilook.IntermediaryFabriclook;
import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;

import java.util.ArrayList;

@Mixin(Options.class)
public class OptionsMixin {
	@Shadow
	@Final
	@Mutable
	public KeyMapping[] keyMappings;

	@Inject(method = "load", at = @At("HEAD"))
	public void onLoad(CallbackInfo ci) {
		IntermediaryFabriclook fl = (IntermediaryFabriclook) Omnilook.getInstanceOrNull();
		if(fl != null) {
			ArrayList<KeyMapping> kbs = Lists.newArrayList(this.keyMappings);
			kbs.remove(fl.key);
			kbs.add(fl.key);
			this.keyMappings = kbs.toArray(new KeyMapping[0]);
		}
	}
}
