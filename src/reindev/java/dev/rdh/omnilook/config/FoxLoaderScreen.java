package dev.rdh.omnilook.config;

import net.minecraft.src.client.gui.GuiButton;
import net.minecraft.src.client.gui.GuiScreen;
import net.minecraft.src.client.gui.GuiSmallButton;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FoxLoaderScreen extends GuiScreen {
	private final GuiScreen parent;

	private final Map<GuiButton, Consumer<GuiButton>> buttons = new HashMap<>();

	public FoxLoaderScreen(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		int buttonWidth = 150;
		int buttonHeight = 20;
		int buttonX = (width - buttonWidth) / 2;
		int buttonY = (height - buttonHeight) / 2 - 50;

		addButton(
				new GuiSmallButton(0, buttonX, buttonY, "Toggle Mode: " + (Config.toggleMode ? "ON" : "OFF")),
				button -> {
					Config.toggleMode = !Config.toggleMode;
					button.displayString = "Toggle Mode: " + (Config.toggleMode ? "ON" : "OFF");
				}
		);

		addButton(
				new GuiSmallButton(1, buttonX, this.height - buttonHeight - 20, "Done"),
				button -> mc.displayGuiScreen(parent)
		);
	}

	private void addButton(GuiButton button, Consumer<GuiButton> action) {
		buttons.put(button, action);
		controlList.add(button);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		Consumer<GuiButton> action = buttons.get(button);
		if (action != null) {
			action.accept(button);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, "Omnilook Config", width / 2, 20, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Config.saveConfig();
	}
}
