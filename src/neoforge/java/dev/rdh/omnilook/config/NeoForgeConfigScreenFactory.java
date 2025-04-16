package dev.rdh.omnilook.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static dev.rdh.omnilook.Neolook.makeConfigScreen;

public class NeoForgeConfigScreenFactory implements IConfigScreenFactory {
	public Screen createScreen(ModContainer modContainer, Screen screen) {
		return makeConfigScreen(modContainer, screen);
	}

	public Screen createScreen(Minecraft minecraft, Screen screen) {
		return makeConfigScreen(minecraft, screen);
	}
}
