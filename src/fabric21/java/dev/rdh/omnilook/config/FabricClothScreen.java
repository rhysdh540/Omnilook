package dev.rdh.omnilook.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FabricClothScreen {
	public static Screen make(Screen parent) {
		ConfigBuilder b = ConfigBuilder.create()
				.setTitle(text("Omnilook"))
				.setSavingRunnable(Config::saveConfig)
				.setParentScreen(parent);

		b.getOrCreateCategory(text("Omnilook")).addEntry(
				b.entryBuilder()
						.startBooleanToggle(text("Toggle Mode"), Config.toggleMode)
						.setDefaultValue(Config.toggleMode)
						.setTooltip(text("If true, pressing the keybind toggles freelook, otherwise it must be held"))
						.setSaveConsumer(value -> Config.toggleMode = value)
						.build()
		);

		return b.build();
	}

	private static Component text(String s) {
		try {
			return Component.nullToEmpty(s);
		} catch (Throwable t1) {
			try {
				return Component.literal(s);
			} catch (Throwable t2) {
				try {
					Class<?> c = Class.forName(FabricLoader.getInstance().getMappingResolver().mapClassName(
							"intermediary",
							"net.minecraft.class_2585"
					));

					return (Component) c.getConstructor(String.class).newInstance(s);
				} catch (Throwable t3) {
					Throwable t = new IllegalStateException("Could not create text component");
					t.addSuppressed(t1);
					t.addSuppressed(t2);
					t.addSuppressed(t3);
					throw t;
				}
			}
		}
	}
}
