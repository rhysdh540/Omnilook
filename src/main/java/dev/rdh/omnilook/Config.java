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

	static void thread() {
		Path parent = Omnilook.getInstance().getConfigDir();
		Path file = parent.resolve("omnilook.properties");
		Semaphore semaphore = new Semaphore(1);
		long nextTime = 0;

		loadConfig(file, semaphore, true);

		WatchService watchService = FileSystems.getDefault().newWatchService();
		parent.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
		while (true) {
			WatchKey key;
			try {
				key = watchService.take();
			} catch (Throwable e) {
				Omnilook.log.error("Error in config: ", e);
				return;
			}

			for(WatchEvent<?> pollEvent : key.pollEvents()) {
				try {
					Path changed = parent.resolve((Path) pollEvent.context());
					if(!Files.isSameFile(changed, file)) {
						Omnilook.log.info("Changed file: {}", changed);
						continue;
					}
					long currentTime = System.currentTimeMillis();
					if(currentTime <= nextTime || !semaphore.tryAcquire()) continue;
					semaphore.release();

					loadConfig(file, semaphore, false);

					nextTime = currentTime + 500L;
					break;
				} catch (IOException e) {
					if(!(e instanceof NoSuchFileException)) {
						Omnilook.log.error("Error in config: ", e);
					}
				}
			}

			if(!key.reset()) {
				Omnilook.log.error("Config watch key is invalid");
				return;
			}
		}
	}

	private static void loadConfig(Path file, Semaphore semaphore, boolean forceRewrite) {
		semaphore.acquireUninterruptibly();
		Properties props = new Properties();

		if(Files.exists(file)) {
			props.load(Files.newBufferedReader(file));
		} else {
			Omnilook.log.info("Config file not found; rewriting...");
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
			props.store(Files.newBufferedWriter(file), " Omnilook Configuration\n" +
					" toggleMode: if true, pressing the keybind toggles freelook, otherwise it must be held"
			);
		}
		Omnilook.log.info("Config loaded: {}", props);
		semaphore.release();
	}
}
