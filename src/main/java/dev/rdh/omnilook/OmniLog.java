package dev.rdh.omnilook;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class OmniLog {
	private static Consumer<String> info, warn, error;
	private static BiConsumer<String, Throwable> errorWithThrowable;

	public static void init(String backendName, Consumer<String> info, Consumer<String> warn, Consumer<String> error, BiConsumer<String, Throwable> errorWithThrowable) {
		if(OmniLog.info != null || OmniLog.warn != null || OmniLog.error != null || OmniLog.errorWithThrowable != null) {
			error("OmniLog already initialized!");
			return;
		}

		OmniLog.info = Objects.requireNonNull(info);
		OmniLog.warn = Objects.requireNonNull(warn);
		OmniLog.error = Objects.requireNonNull(error);
		OmniLog.errorWithThrowable = Objects.requireNonNull(errorWithThrowable);

		info("OmniLog initialized with " + backendName + " backend");
	}

	public static void info(String message) {
		info.accept(message);
	}

	public static void warn(String message) {
		warn.accept(message);
	}

	public static void error(String message) {
		error.accept(message);
	}

	public static void error(String message, Throwable throwable) {
		errorWithThrowable.accept(message, throwable);
	}
}
