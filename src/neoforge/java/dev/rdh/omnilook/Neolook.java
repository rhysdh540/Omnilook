package dev.rdh.omnilook;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

import dev.rdh.omnilook.config.Config;
import dev.rdh.omnilook.config.NeoForgeClothScreen;
import dev.rdh.omnilook.config.NeoForgeYACLScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Mod(value = Omnilook.ID, dist = Dist.CLIENT)
public final class Neolook extends BaseMojmapImpl {
	@SuppressWarnings({"unchecked", "rawtypes"})
	public Neolook(IEventBus bus) {
		NeoForge.EVENT_BUS.addListener(this::onComputeCameraAngles);
		bus.addListener(this::onRegisterKeyMappings);

		Class extensionPoint;
		Object extension;
		try {
			extensionPoint = Class.forName("net.neoforged.neoforge.client.gui.IConfigScreenFactory");
			extension = Proxy.newProxyInstance(Neolook.class.getClassLoader(),
					new Class[]{extensionPoint},
					(proxy, method, args) -> method.getName().equals("createScreen")
							? makeConfigScreen(args[0], (Screen) args[1])
							: method.invoke(proxy, args));
		} catch (ClassNotFoundException e) {
			extensionPoint = Class.forName("net.neoforged.neoforge.client.ConfigScreenHandler$ConfigScreenFactory");
			extension = extensionPoint.getDeclaredConstructor(BiFunction.class)
					.newInstance((BiFunction<Minecraft, Screen, Screen>) this::makeConfigScreen);
		}
		Object fextension = extension;
		ModLoadingContext.get().registerExtensionPoint(extensionPoint, (Supplier) () -> fextension);
	}

	private Screen makeConfigScreen(Object arg, Screen parent) {
		if (MixinPlugin.classExists("dev.isxander.yacl3.api.YetAnotherConfigLib")) {
			try {
				return NeoForgeYACLScreen.make(parent);
			} catch (Throwable ignored) {
			}
		}

		if (MixinPlugin.classExists("me.shedaniel.clothconfig2.api.ConfigBuilder")) {
			try {
				return NeoForgeClothScreen.make(parent);
			} catch (Throwable ignored) {
			}
		}

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
}
