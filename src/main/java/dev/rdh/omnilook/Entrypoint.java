package dev.rdh.omnilook;

import net.fabricmc.api.ClientModInitializer;

@net.minecraftforge.fml.common.Mod(
		value = Omnilook.ID,
		modid = Omnilook.ID,
		useMetadata = true
)
@cpw.mods.fml.common.Mod(
		modid = Omnilook.ID,
		useMetadata = true
)
@SuppressWarnings("IfCanBeSwitch")
public final class Entrypoint {
	public Entrypoint() {
		String classname = MixinPlugin.getPlatform();
		if(classname.equals("LexForge")) {
			classname = "dev.rdh.omnilook.Forgelook";
		} else if(classname.equals("LexForge16") || classname.equals("LexForge13")) {
			classname = "dev.rdh.omnilook.Forgelook16";
		} else if(classname.equals("LexForge12")) {
			classname = "dev.rdh.omnilook.Forgelook12";
		} else {
			throw new IllegalStateException("Unexpected platform: " + classname);
		}

		Class.forName(classname).getDeclaredConstructor().newInstance();
	}

	public static void fabric() {
		String classname;
		String platform = MixinPlugin.getPlatform();
		if(platform.equals("Fabric")) {
			classname = "dev.rdh.omnilook.Fabriclook";
		} else {
			throw new IllegalStateException("Unexpected platform: " + platform);
		}

		ClientModInitializer cmi = (ClientModInitializer) Class.forName(classname).getDeclaredConstructor().newInstance();
		cmi.onInitializeClient();
	}
}
