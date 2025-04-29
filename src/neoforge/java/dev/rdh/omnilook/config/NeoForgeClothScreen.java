package dev.rdh.omnilook.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NeoForgeClothScreen {
	public static Screen make(Screen parent) {
		ConfigBuilder b = ConfigBuilder.create()
				.setTitle(Component.literal("Omnilook"))
				.setSavingRunnable(Config::saveConfig)
				.setParentScreen(parent);

		b.getOrCreateCategory(Component.literal("Omnilook")).addEntry(
				b.entryBuilder()
						.startBooleanToggle(Component.literal("Toggle Mode"), Config.toggleMode)
						.setDefaultValue(Config.toggleMode)
						.setTooltip(Component.literal("If true, pressing the keybind toggles freelook, otherwise it must be held"))
						.setSaveConsumer(value -> Config.toggleMode = value)
						.build()
		);

		return b.build();
	}
}
