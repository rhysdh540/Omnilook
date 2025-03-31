package dev.rdh.omnilook;

import com.fox2code.foxloader.client.KeyBindingAPI;
import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.loader.Mod;

public class FoxlookMod extends Mod implements ClientMod {
	private final Foxlook impl = new Foxlook();

	@Override
	public void onPreInit() {
		KeyBindingAPI.registerKeyBinding(impl.key);
	}
}
