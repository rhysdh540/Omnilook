package dev.rdh.omnilook;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions("dev.rdh.omnilook.MixinPlugin")
public class NoTransform implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> map) {

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
