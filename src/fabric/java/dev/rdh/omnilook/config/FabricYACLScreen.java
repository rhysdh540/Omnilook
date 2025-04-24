package dev.rdh.omnilook.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;

import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FabricYACLScreen {
	public static Screen make(Screen parent) {
		return YetAnotherConfigLib.createBuilder()
				.title(Component.nullToEmpty("Omnilook"))
				.category(ConfigCategory.createBuilder()
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
}
