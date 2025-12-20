package dev.rdh.omnilook;

import dev.rdh.omnilook.config.Config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.function.Consumer;

public final class Forgelook extends BaseMojmapImpl {
	public Forgelook() {
		if(FMLEnvironment.dist != Dist.CLIENT) {
			Omnilook.LOGGER.error("Omnilook is a client-side mod and cannot be loaded on a server.");
			return;
		}

		FMLModContainer c = (FMLModContainer) ModList.get().getModContainerById(ID)
				.orElseThrow(IllegalStateException::new);

		try {
			// pre-1.21.6
			c.getEventBus().addListener(this::onRegisterKeyMappings);
		} catch (Throwable t) {
			Object bus;
			try {
				// post-1.21.9
				bus = RegisterKeyMappingsEvent.class.getDeclaredField("BUS").get(null);
			} catch (Throwable t2) {
				// 1.21.6 - 1.21.8
				Object busGroup = FMLModContainer.class.getDeclaredMethod("getModBusGroup").invoke(c);
				bus = RegisterKeyMappingsEvent.class.getDeclaredMethod("getBus", busGroup.getClass())
						.invoke(null, busGroup);
			}

			bus.getClass().getDeclaredMethod("addListener", Consumer.class)
					.invoke(bus, (Consumer<RegisterKeyMappingsEvent>) this::onRegisterKeyMappings);
		}

		MinecraftForge.registerConfigScreen(parent -> {
			Config.openTextEditor();
			return null;
		});
	}

	void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(key);
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}
}
