package dev.rdh.omnilook.config;

import com.mumfrey.liteloader.modconfig.AbstractConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;

import net.minecraft.client.gui.GuiButton;

public class LiteScreen extends AbstractConfigPanel {
	@Override
	public String getPanelTitle() {
		return "Omnilook";
	}

	@Override
	protected void addOptions(ConfigPanelHost host) {
		int screenWidth = host.getWidth();
		int screenHeight = host.getHeight();
		int buttonWidth = 200;
		int buttonHeight = 20;

		int buttonX = (screenWidth - buttonWidth) / 2;
		int buttonY = (screenHeight - buttonHeight) / 2 - 50;
		String initialText = Config.toggleMode ? "Toggle Mode: ON" : "Toggle Mode: OFF";
		addLabel(0, buttonX, buttonY - 20, 0, 20, 0xFFFFFF, "Omnilook Config");
		addControl(new GuiButton(1, buttonX, buttonY, buttonWidth, buttonHeight, initialText), b -> {
			Config.toggleMode = !Config.toggleMode;
			if (Config.toggleMode) {
				b.displayString = "Toggle Mode: ON";
			} else {
				b.displayString = "Toggle Mode: OFF";
			}
		});
	}

	@Override
	public void onPanelHidden() {
		Config.saveConfig();
	}
}
