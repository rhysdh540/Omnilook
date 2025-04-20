package dev.rdh.omnilook.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NeoForgeScreens {
	public static Screen yacl(Screen parent) {
		return YetAnotherConfigLib.createBuilder()
				.title(Component.literal("Omnilook"))
				.category(ConfigCategory.createBuilder()
						.name(Component.literal("Omnilook"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Toggle Mode"))
								.controller(TickBoxControllerBuilder::create)
								.description(OptionDescription.of(Component.literal("If true, pressing the keybind toggles freelook, otherwise it must be held")))
								.binding(Config.toggleMode, () -> Config.toggleMode, value -> Config.toggleMode = value)
								.build()
						)
						.build()
				)
				.save(Config::saveConfig)
				.build()
				.generateScreen(parent);
	}

	public static Screen cloth(Screen parent) {
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
