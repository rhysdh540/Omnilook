package dev.rdh.omnilook;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import dev.rdh.omnilook.config.Config;
import dev.rdh.omnilook.config.NeoForgeClothScreen;
import dev.rdh.omnilook.config.NeoForgeConfigScreenFactory;
import dev.rdh.omnilook.config.NeoForgeYACLScreen;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Mod(value = Omnilook.ID, dist = Dist.CLIENT)
public final class Neolook extends Omnilook {
	private final KeyMapping key;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Neolook(IEventBus bus) {
		key = new KeyMapping(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KEYBINDING_CATEGORY);
		NeoForge.EVENT_BUS.addListener(this::onComputeCameraAngles);
		bus.addListener(this::onRegisterKeyMappings);

		Class extensionPoint;
		Object extension;
		try {
			extensionPoint = Class.forName("net.neoforged.neoforge.client.gui.IConfigScreenFactory");
			extension = new NeoForgeConfigScreenFactory();
		} catch (ClassNotFoundException e) {
			extensionPoint = Class.forName("net.neoforged.neoforge.client.ConfigScreenHandler$ConfigScreenFactory");
			extension = extensionPoint.getDeclaredConstructor(BiFunction.class)
					.newInstance((BiFunction<Minecraft, Screen, Screen>) Neolook::makeConfigScreen);
		}
		Object fextension = extension;
		ModLoadingContext.get().registerExtensionPoint(extensionPoint, (Supplier) () -> fextension);
	}

	public static Screen makeConfigScreen(Object arg, Screen parent) {
		if (MixinPlugin.classExists("dev.isxander.yacl3.api.YetAnotherConfigLib")) {
			return NeoForgeYACLScreen.make(parent);
		}

		if (MixinPlugin.classExists("me.shedaniel.clothconfig2.api.ConfigBuilder")) {
			return NeoForgeClothScreen.make(parent);
		}

		OmniLog.warn("No screen providers found");
		Config.openTextEditor();
		return null;
	}

	void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(key);
	}

	void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
		this.update();

		if (this.isEnabled()) {
			event.setPitch(getXRot());
			event.setYaw(getYRot());
		}
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft.getInstance().options.setCameraType(CameraType.values()[cameraType]);
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getInstance().options.getCameraType().ordinal();
	}

	@Override
	protected float getMCXRot() {
		return Minecraft.getInstance().cameraEntity.getXRot();
	}

	@Override
	protected float getMCYRot() {
		return Minecraft.getInstance().cameraEntity.getYRot();
	}

	@Override
	protected boolean isKeyClicked() {
		return key.consumeClick();
	}

	@Override
	protected boolean isKeyDown() {
		return key.isDown();
	}
}
