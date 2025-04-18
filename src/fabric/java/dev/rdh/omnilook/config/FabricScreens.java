package dev.rdh.omnilook.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;

import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.rdh.omnilook.Config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FabricScreens {
	public static Screen yacl(Screen parent) {
		return YetAnotherConfigLib.createBuilder()
				.title(Component.nullToEmpty("Omnilook"))
				.category(dev.isxander.yacl3.api.ConfigCategory.createBuilder()
						.name(Component.nullToEmpty("Omnilook"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.nullToEmpty("Toggle Mode"))
								.controller(TickBoxControllerBuilder::create)
								.description(OptionDescription.of(Component.nullToEmpty("If true, pressing the keybind toggles freelook, otherwise it must be held")))
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
				.setTitle(Component.nullToEmpty("Omnilook"))
				.setSavingRunnable(Config::saveConfig)
				.setParentScreen(parent);

		me.shedaniel.clothconfig2.api.ConfigCategory category = b.getOrCreateCategory(Component.nullToEmpty("Omnilook"));
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
}
