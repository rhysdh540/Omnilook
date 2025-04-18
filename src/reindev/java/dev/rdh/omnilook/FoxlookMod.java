package dev.rdh.omnilook;

import com.fox2code.foxloader.client.KeyBindingAPI;
import com.fox2code.foxloader.client.gui.GuiConfigProvider;
import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.loader.Mod;

import dev.rdh.omnilook.config.FoxLoaderScreen;

import net.minecraft.src.client.gui.GuiScreen;

public class FoxlookMod extends Mod implements ClientMod, GuiConfigProvider {
	private final Foxlook impl = new Foxlook();

	@Override
	public void onPreInit() {
		KeyBindingAPI.registerKeyBinding(impl.key);

		this.setConfigObject(this);
	}

	@Override
	public GuiScreen provideConfigScreen(GuiScreen parent) {
		return new FoxLoaderScreen(parent);
	}
}
