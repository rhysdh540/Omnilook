package dev.rdh.omnilook.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.rdh.omnilook.MixinPlugin;
import dev.rdh.omnilook.OmniLog;

public class ModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if(MixinPlugin.classExists("dev.isxander.yacl3.api.YetAnotherConfigLib")) {
			return FabricYACLScreen::make;
		}

		if(MixinPlugin.classExists("me.shedaniel.clothconfig2.api.ConfigBuilder")) {
			return FabricClothScreen::make;
		}

		OmniLog.error("No screen providers found");
		return parent -> {
			Config.openTextEditor();
			return null;
		};
	}
}
