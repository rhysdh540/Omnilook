package dev.rdh.omnilook;

import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.service.MixinService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

public interface OmniLog {
	void info(String message, Object... args);
	void warn(String message, Object... args);
	void debug(String message, Object... args);
	void error(String message, Object... args);
	void error(String message, Throwable t);

	static OmniLog get(String name) {
		Object delegate;
		try {
			delegate = LogManager.getLogger(name);
		} catch (Throwable t1) {
			try {
				delegate = MixinService.getService().getLogger(name);
			} catch (Throwable t2) {
				try {
					Function<String, ?> constructor = (Function<String, ?>) System.getProperties().get("ol.log");
					if (constructor == null) throw new IllegalStateException("Nothing under `ol.log`");
					delegate = constructor.apply(name);
				} catch (Throwable t3) {
					Throwable t = new IllegalStateException("Failed to acquire logger");
					t.addSuppressed(t1);
					t.addSuppressed(t2);
					t.addSuppressed(t3);
					throw t;
				}
			}
		}

		final Object d = delegate;
		return (OmniLog) Proxy.newProxyInstance(
				OmniLog.class.getClassLoader(),
				new Class[]{OmniLog.class},
				(_, method, args) -> {
					Method m;
					try {
						m = d.getClass().getMethod(method.getName(), method.getParameterTypes());
					} catch (NoSuchMethodException e) {
						Class<?>[] pt = method.getParameterTypes();
						if ("error".equals(method.getName()) && pt.length == 2 && pt[0] == String.class && pt[1] == Throwable.class) {
							try {
								m = d.getClass().getMethod("error", String.class, Object[].class);
								args = new Object[]{args[0], new Object[]{args[1]}};
							} catch (NoSuchMethodException e2) {
								e.addSuppressed(e2);
								throw e;
							}
						} else {
							throw e;
						}
					}

					return m.invoke(d, args);
				}
		);
	}
}
