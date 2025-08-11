package dev.rdh.omnilook.config;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;

import dev.rdh.omnilook.Omnilook;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class LexForge7Screen extends GuiConfig implements IModGuiFactory {
	private final Map<Property, Consumer<String>> configs =
			ImmutableMap.<Property, Consumer<String>>builder()
					.put(new Property("Toggle Mode", String.valueOf(Config.toggleMode), Property.Type.BOOLEAN),
							(value) -> Config.toggleMode = Boolean.parseBoolean(value))
					.build();

	public LexForge7Screen(Screen parent) {
		super(parent, new ArrayList<>(), Omnilook.ID, false, false, "Omnilook");

		for(Property prop : configs.keySet()) {
			configElements.add(new ConfigElement<>(prop));
		}

	}

	@Override
	public void removed() {
		super.removed();
		for(Map.Entry<Property, Consumer<String>> entry : configs.entrySet()) {
			Property property = entry.getKey();
			Consumer<String> consumer = entry.getValue();
			String value = property.getString();
			consumer.accept(value);
		}
		Config.saveConfig();
	}

	public LexForge7Screen() {
		super(null, new ArrayList<>(), "", false, false, "");
	}

	@Override
	public Class<? extends Screen> mainConfigGuiClass() {
		return LexForge7Screen.class;
	}

	@Override
	public void initialize(Minecraft mc) {
		// no-op
	}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement runtimeOptionCategoryElement) {
		return null;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
}
