package dev.rdh.omnilook.config;

import org.polyfrost.oneconfig.api.config.v1.ConfigManager;
import org.polyfrost.oneconfig.api.config.v1.Properties;
import org.polyfrost.oneconfig.api.config.v1.Property;
import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.api.config.v1.Visualizer;
import org.polyfrost.oneconfig.api.config.v1.backend.Backend;

import dev.rdh.omnilook.Omnilook;

public final class OneConfigScreen {
	private static Tree tree;

	public static void init() {
		try {
			Backend.class.getField("UI_ONLY_METADATA");
		} catch (NoSuchFieldException e) {
			Omnilook.LOGGER.warn("OneConfig 1.3.0 or newer is required for its config screen");
			return;
		}
		Tree config = Tree.tree(Omnilook.ID);
		config.setTitle("Omnilook");
		config.addMetadata(Backend.UI_ONLY_METADATA, true);
		Property<Boolean> toggle = Properties.functional(() -> Config.toggleMode, value -> {
			Config.toggleMode = value;
			Config.saveConfig();
		}, "toggleMode", "Toggle Mode", "If true, pressing the keybind toggles freelook, otherwise it must be held", Boolean.class);
		toggle.addMetadata("visualizer", Visualizer.SwitchVisualizer.class);
		toggle.addMetadata("default", true);
		config.put(toggle);
		tree = ConfigManager.active().register(config).get();
	}

	@SuppressWarnings("unchecked")
	public static <S> S make() {
		if(tree == null) return null;
		try {
			return (S) Class.forName("org.polyfrost.oneconfig.utils.v1.dsl.ScreensKt")
					.getMethod("createScreen", Tree.class).invoke(null, tree);
		} catch (ReflectiveOperationException e) {
			Omnilook.LOGGER.warn("Could not open the OneConfig screen", e);
			return null;
		}
	}
}
