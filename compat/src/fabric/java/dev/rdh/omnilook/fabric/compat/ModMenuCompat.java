package dev.rdh.omnilook.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.rdh.omnilook.MixinPlugin;
import dev.rdh.omnilook.OmniLog;

public class ModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if(MixinPlugin.classExists("dev.isxander.yacl3.api.YetAnotherConfigLib")) {
			return YaclConfigProvider::make;
		}

		OmniLog.error("No screen providers found");
		return ModMenuApi.super.getModConfigScreenFactory();
	}
}
