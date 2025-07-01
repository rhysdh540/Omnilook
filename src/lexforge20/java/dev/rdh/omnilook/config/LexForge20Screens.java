package dev.rdh.omnilook.config;

import dev.rdh.omnilook.MixinPlugin;
import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;

public class LexForge20Screens {
	public static Screen cloth(Screen parent) {
		ConfigBuilder b = ConfigBuilder.create()
				.setTitle(Component.nullToEmpty("Omnilook"))
				.setSavingRunnable(Config::saveConfig)
				.setParentScreen(parent);

		ConfigCategory category = b.getOrCreateCategory(Component.nullToEmpty("Omnilook"));
		category.addEntry(
				b.entryBuilder()
						.startBooleanToggle(Component.nullToEmpty("Toggle Mode"), Config.toggleMode)
						.setDefaultValue(Config.toggleMode)
						.setTooltip(Component.nullToEmpty("If true, pressing the keybind toggles freelook, otherwise it must be held"))
						.setSaveConsumer(value -> Config.toggleMode = value)
						.build()
		);

		return b.build();
	}

	public static Screen make(Screen parent) {
		if (MixinPlugin.classExists("me.shedaniel.clothconfig2.api.ConfigBuilder")) {
			return cloth(parent);
		}

		Omnilook.LOGGER.warn("No screen providers found");
		Config.openTextEditor();
		return null;
	}
}
