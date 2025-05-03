package dev.rdh.omnilook;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class OmniLog {
	private static final Consumer<String> info, warn, error;
	private static final BiConsumer<String, Throwable> errorWithThrowable;

	static {
		String backendName;
		if(MixinPlugin.classExists("org.apache.logging.log4j.Logger")) {
			org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("Omnilook");
			info = logger::info;
			warn = logger::warn;
			error = logger::error;
			errorWithThrowable = logger::error;
			backendName = "Log4j2";
		} else if(MixinPlugin.classExists("org.slf4j.Logger")) {
			org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("Omnilook");
			info = logger::info;
			warn = logger::warn;
			error = logger::error;
			errorWithThrowable = logger::error;
			backendName = "SLF4J";
		} else {
			info = s -> System.out.println("[Omnilook/INFO] " + s);
			warn = s -> System.out.println("[Omnilook/WARN] " + s);
			error = s -> System.err.println("[Omnilook/ERROR] " + s);
			errorWithThrowable = (s, e) -> {
				error.accept(s);
				e.printStackTrace();
			};
			backendName = "fallback";
		}

		info("OmniLog initialized with " + backendName + " backend");
	}

	public static void info(Object message) {
		info.accept(String.valueOf(message));
	}

	public static void warn(Object message) {
		warn.accept(String.valueOf(message));
	}

	public static void error(Object message) {
		error.accept(String.valueOf(message));
	}

	public static void error(Object message, Throwable throwable) {
		errorWithThrowable.accept(String.valueOf(message), throwable);
	}
}
