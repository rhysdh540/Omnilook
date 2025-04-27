package dev.rdh.omnilook.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.rdh.omnilook.OmniLog;

// TODO: make this work with the old io.github.prospector.modmenu package
//  (used on old versions of modmenu + some legacy forks)
public class ModMenuCompat implements ModMenuApi {

	public ModMenuCompat() {
		OmniLog.info("Loading Mod Menu compat");
	}

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ModMenuScreenProvider::getScreen;
	}
}
