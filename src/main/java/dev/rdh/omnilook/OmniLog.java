package dev.rdh.omnilook;

import org.spongepowered.asm.service.MixinService;

import java.lang.reflect.Proxy;

public interface OmniLog {
	void info(String message, Object... args);
	void warn(String message, Object... args);
	void debug(String message, Object... args);
	void error(String message, Object... args);
	void error(String message, Throwable t);

	static OmniLog get(String name) {
		Object delegate;
		try {
			delegate = MixinService.getService().getLogger(name);
		} catch (Throwable t1) {
			try {
				delegate = Class.forName("org.apache.logging.log4j.LogManager")
						.getMethod("getLogger", String.class)
						.invoke(null, name);
			} catch (Throwable t2) {
				Throwable t = new IllegalStateException("Failed to acquire logger");
				t.addSuppressed(t1);
				t.addSuppressed(t2);
				throw t;
			}
		}

		final Object d = delegate;
		return (OmniLog) Proxy.newProxyInstance(
				OmniLog.class.getClassLoader(),
				new Class[]{OmniLog.class},
				(proxy, method, args) ->
						d.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(d, args)
		);
	}
}
