package dev.rdh.omnilook.config;

import com.google.common.collect.ImmutableMap;

import dev.rdh.omnilook.Omnilook;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class LexForge12Screen extends GuiConfig implements IModGuiFactory {
	private final Map<Property, Consumer<String>> configs =
			ImmutableMap.<Property, Consumer<String>>builder()
					.put(new Property("Toggle Mode", String.valueOf(Config.toggleMode), Property.Type.BOOLEAN),
							(value) -> Config.toggleMode = Boolean.parseBoolean(value))
					.build();

	public LexForge12Screen(Screen parent) {
		super(parent, Collections.emptyList(), Omnilook.ID, false, false, "Omnilook");

		for(Property prop : configs.keySet()) {
			configElements.add(new ConfigElement(prop));
		}

	}

	@Override
	public void removed() {
		super.removed();
		for(Map.Entry<Property, Consumer<String>> entry : configs.entrySet()) {
			entry.getValue().accept(entry.getKey().getString());
		}
		Config.saveConfig();
	}

	public LexForge12Screen() {
		super(null, Collections.emptyList(), null, false, false, null);
	}

	@Override
	public void initialize(Minecraft mc) {
		// no-op
	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public Screen createConfigGui(Screen guiScreen) {
		return new LexForge12Screen(guiScreen);
	}

	public Class<? extends Screen> mainConfigGuiClass() {
		return LexForge12Screen.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
}
