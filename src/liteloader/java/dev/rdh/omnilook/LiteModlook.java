package dev.rdh.omnilook;

import com.mumfrey.liteloader.Configurable;
import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import org.lwjgl.input.Keyboard;

import dev.rdh.omnilook.config.LiteScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.nio.file.Path;

// liteloader forces mods to start their entrypoint class name with `LiteMod`, do not change
public class LiteModlook extends Omnilook implements LiteMod, Configurable {
	private final KeyBinding key;

	public LiteModlook() {
		key = new KeyBinding(KEYBINDING_NAME, Keyboard.KEY_GRAVE, KEYBINDING_CATEGORY);
	}

	@Override
	public String getVersion() {
		return null; // version from litemod.json will be used instead
	}

	@Override
	public String getName() {
		return "Omnilook";
	}

	@Override
	public void init(File configPath) {
		LiteLoader.getInput().registerKeyBinding(key);
	}

	@Override
	public Class<? extends ConfigPanel> getConfigPanelClass() {
		return LiteScreen.class;
	}

	@Override
	public Path getConfigDir() {
		return Launch.minecraftHome.toPath().resolve("config");
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft.getMinecraft().gameSettings.thirdPersonView = cameraType;
		Minecraft.getMinecraft().entityRenderer.loadEntityShader(cameraType == 0 ? Minecraft.getMinecraft().getRenderViewEntity() : null);

		Minecraft.getMinecraft().renderGlobal.setDisplayListEntitiesDirty();
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getMinecraft().gameSettings.thirdPersonView;
	}

	@Override
	protected float getMCXRot() {
		return Minecraft.getMinecraft().getRenderViewEntity().rotationPitch;
	}

	@Override
	protected float getMCYRot() {
		return Minecraft.getMinecraft().getRenderViewEntity().rotationYaw;
	}

	@Override
	protected boolean isKeyClicked() {
		return key.isPressed();
	}

	@Override
	protected boolean isKeyDown() {
		return key.isKeyDown();
	}

	@Override
	public void upgradeSettings(String s, File file, File file1) {
	}
}
