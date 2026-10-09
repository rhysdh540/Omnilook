package cpw.mods.modlauncher.api;

import java.util.Optional;
import java.util.function.Supplier;

public interface IEnvironment {
	<T> Optional<T> getProperty(TypesafeMap.Key<T> key);

	Optional<ILaunchHandlerService> findLaunchHandler(String target);

	final class Keys {
		public static final Supplier<TypesafeMap.Key<String>> LAUNCHTARGET = () -> null;
	}
}
