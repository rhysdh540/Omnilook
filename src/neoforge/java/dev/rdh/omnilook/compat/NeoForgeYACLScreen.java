package dev.rdh.omnilook.compat;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.rdh.omnilook.Config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NeoForgeYACLScreen {
	public static Screen make(Screen parent) {
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
}
