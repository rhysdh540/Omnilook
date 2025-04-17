package dev.rdh.omnilook;

@net.minecraftforge.fml.common.Mod(
		value = Omnilook.ID,
		modid = Omnilook.ID,
		guiFactory = "dev.rdh.omnilook.config.LexForge12Screen",
		useMetadata = true
)
@cpw.mods.fml.common.Mod(
		modid = Omnilook.ID,
		guiFactory = "dev.rdh.omnilook.config.LexForge7Screen",
		useMetadata = true
)
@SuppressWarnings("IfCanBeSwitch")
public final class Entrypoint {
	public Entrypoint() {
		String classname = MixinPlugin.getPlatform();
		if(classname.equals("LexForge")) {
			classname = "dev.rdh.omnilook.Forgelook";
		} else if(classname.equals("LexForge20")) {
			classname = "dev.rdh.omnilook.Forgelook20";
		} else if(classname.equals("LexForge16") || classname.equals("LexForge13")) {
			classname = "dev.rdh.omnilook.Forgelook16";
		} else if(classname.equals("LexForge12")) {
			classname = "dev.rdh.omnilook.Forgelook12";
		} else if(classname.equals("Fabric")) {
			classname = "dev.rdh.omnilook.Fabriclook";
		} else if(classname.equals("LegacyFabric")) {
			classname = "dev.rdh.omnilook.LegacyFabriclook";
		} else if(classname.equals("Babric")) {
			classname = "dev.rdh.omnilook.Babriclook";
		} else {
			throw new IllegalStateException("Unexpected platform: " + classname);
		}

		Class.forName(classname).getDeclaredConstructor().newInstance();
	}

	public void fabric() {
		// no-op, fabric will call the constructor above and that does the actual stuff
	}
}
