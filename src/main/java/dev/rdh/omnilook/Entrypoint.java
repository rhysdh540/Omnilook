package dev.rdh.omnilook;

import net.fabricmc.api.ClientModInitializer;

import net.minecraftforge.fml.common.Mod;

@Mod(
		value = Omnilook.ID,
		modid = Omnilook.ID
)
public final class Entrypoint {
	public Entrypoint() {
		String classname;
		if(MixinPlugin.getPlatform().equals("LexForge")) {
			classname = "dev.rdh.omnilook.Forgelook";
		} else if(MixinPlugin.getPlatform().equals("LexForge16")) {
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

	public static void fabric() {
		String classname;
		if(MixinPlugin.getPlatform().equals("Fabric")) {
			classname = "dev.rdh.omnilook.Fabriclook";
		} else {
			throw new IllegalStateException("Unexpected platform: " + MixinPlugin.getPlatform());
		}

		try {
			ClientModInitializer cmi = (ClientModInitializer) Class.forName(classname).getDeclaredConstructor().newInstance();
			cmi.onInitializeClient();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
