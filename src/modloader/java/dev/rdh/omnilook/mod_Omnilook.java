package dev.rdh.omnilook;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseMod;

public class mod_Omnilook extends BaseMod {
	public final ModLoaderlook omnilook;

	public mod_Omnilook() {
		this.omnilook = new ModLoaderlook();
	}

	@Override
	public String getVersion() {
		return null;
	}

	@Override
	public boolean onTickInGame(float tick, Minecraft mc) {
		omnilook.update();
		return super.onTickInGame(tick, mc);
	}

	@Override
	public void load() {

	}
}
