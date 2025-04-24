package dev.rdh.omnilook.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FabricClothScreen {
	public static Screen make(Screen parent) {
		ConfigBuilder b = ConfigBuilder.create()
				.setTitle(Component.nullToEmpty("Omnilook"))
				.setSavingRunnable(Config::saveConfig)
				.setParentScreen(parent);

		b.getOrCreateCategory(Component.nullToEmpty("Omnilook")).addEntry(
				b.entryBuilder()
						.startBooleanToggle(Component.nullToEmpty("Toggle Mode"), Config.toggleMode)
						.setDefaultValue(Config.toggleMode)
						.setTooltip(Component.nullToEmpty("If true, pressing the keybind toggles freelook, otherwise it must be held"))
						.setSaveConsumer(value -> Config.toggleMode = value)
						.build()
		);

		return b.build();
	}
}
