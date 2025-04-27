package dev.rdh.omnilook.config;

import dev.rdh.omnilook.Omnilook;

@SuppressWarnings("unused")
public interface ModMenuScreenProvider<S> {
	default S openScreen(S parent) {
		Config.openTextEditor();
		return null;
	}

	default S openScreen(Object arg1, S parent) {
		return openScreen(parent);
	}

	@SuppressWarnings("unchecked")
	static <S> S getScreen(S parent) {
		Omnilook omnilook = Omnilook.getInstance();
		if(!(omnilook instanceof ModMenuScreenProvider)) {
			throw new IllegalStateException("Omnilook.instance is not a ModMenuScreenProvider");
		}

		ModMenuScreenProvider<S> provider = (ModMenuScreenProvider<S>) omnilook;
		return provider.openScreen(parent);
	}
}
