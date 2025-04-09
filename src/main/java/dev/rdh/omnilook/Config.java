package dev.rdh.omnilook;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Properties;
import java.util.concurrent.Semaphore;

public final class Config {
	public static boolean toggleMode = true;
	private static final Semaphore semaphore = new Semaphore(1);
	private static final Path FILE = Omnilook.getInstance().getConfigDir().resolve("omnilook.properties");

	static void thread() {
		Path parent = FILE.getParent();
		if(!Files.exists(parent)) {
			Files.createDirectories(parent);
		}
		long nextTime = 0;

		loadConfig(true);

		WatchService watchService = FileSystems.getDefault().newWatchService();
		parent.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
		while (true) {
			WatchKey key = watchService.take();

			for(WatchEvent<?> pollEvent : key.pollEvents()) {
				try {
					Path changed = parent.resolve((Path) pollEvent.context());
					if(!Files.isSameFile(changed, FILE)) continue;

					long currentTime = System.currentTimeMillis();
					if(currentTime <= nextTime || !semaphore.tryAcquire()) continue;
					semaphore.release();

					loadConfig(false);

					nextTime = currentTime + 500L;
					break;
				} catch (IOException e) {
					if(!(e instanceof NoSuchFileException)) {
						OmniLog.error("Error in config: ", e);
					}
				}
			}

			if(!key.reset()) {
				OmniLog.error("Config watch key is invalid");
				return;
			}
		}
	}

	public static void saveConfig() {
		Properties props = new Properties();
		props.setProperty("toggleMode", Boolean.toString(toggleMode));

		props.store(Files.newBufferedWriter(FILE), " Omnilook Configuration\n" +
				" toggleMode: if true, pressing the keybind toggles freelook, otherwise it must be held"
		);
	}

	private static void loadConfig(boolean forceRewrite) {
		semaphore.acquireUninterruptibly();
		Properties props = new Properties();

		if(Files.exists(FILE)) {
			props.load(Files.newBufferedReader(FILE));
		} else {
			OmniLog.warn("Config file not found; rewriting...");
			forceRewrite = true;
		}

		//region change config around
		String toggleModeProp = props.getProperty("toggleMode");
		if(toggleModeProp != null) {
			toggleMode = Boolean.parseBoolean(toggleModeProp);
		} else {
			props.setProperty("toggleMode", Boolean.toString(toggleMode));
		}
		//endregion

		if (forceRewrite) {
			saveConfig();
		}
		OmniLog.info("Config loaded: " + props);
		semaphore.release();
	}
}
