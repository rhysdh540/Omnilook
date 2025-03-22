package dev.rdh.omnilook;

import net.minecraftforge.fml.common.Mod;

@Mod(
		value = Omnilook.ID,
		modid = Omnilook.ID
)
public class ForgeEntrypoint {
	public ForgeEntrypoint() {
		String classname;
		if(MixinPlugin.getPlatform().equals("LexForge")) {
			classname = "dev.rdh.omnilook.Forgelook";
		} else if(MixinPlugin.getPlatform().equals("LexForge16") || MixinPlugin.getPlatform().equals("LexForge13")) {
			classname = "dev.rdh.omnilook.Forgelook16";
		} else {
			throw new IllegalStateException("Unexpected platform: " + MixinPlugin.getPlatform());
		}

		try {
			Class.forName(classname).getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
