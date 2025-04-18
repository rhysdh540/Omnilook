package dev.rdh.omnilook.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.rdh.omnilook.Config;
import dev.rdh.omnilook.MixinPlugin;
import dev.rdh.omnilook.OmniLog;

public class ModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if(MixinPlugin.classExists("dev.isxander.yacl3.api.YetAnotherConfigLib")) {
			return FabricScreens::yacl;
		}

		if(MixinPlugin.classExists("me.shedaniel.clothconfig2.api.ConfigBuilder")) {
			return FabricScreens::cloth;
		}

		OmniLog.error("No screen providers found");
		return parent -> {
			Config.openTextEditor();
			return null;
		};
	}
}
