package dev.rdh.omnilook;

import net.fabricmc.loader.api.FabricLoader;

import dev.rdh.omnilook.config.ModMenuScreenProvider;

import net.minecraft.client.gui.screens.Screen;

import java.nio.file.Path;

public final class Fabriclook extends BaseMojmapImpl implements ModMenuScreenProvider<Screen> {
	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}
}
