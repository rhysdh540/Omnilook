package dev.rdh.omnilook.config;

import dev.rdh.omnilook.Omnilook;

public interface ModMenuScreenProvider<S> {
	default S openScreen(S parent) {
		Config.openTextEditor();
		return parent;
	}

	// referenced by generated code!
	@SuppressWarnings({"unchecked", "unused"})
	static <S> S getScreen(S parent) {
		Omnilook omnilook = Omnilook.getInstance();
		if(!(omnilook instanceof ModMenuScreenProvider)) {
			throw new IllegalStateException("Omnilook.instance is not a ModMenuScreenProvider (is " + omnilook.getClass().getName() + ")");
		}

		ModMenuScreenProvider<S> provider = (ModMenuScreenProvider<S>) omnilook;
		return provider.openScreen(parent);
	}
}
