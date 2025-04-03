package dev.rdh.omnilook;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.entities.PlayerPerspective;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.settings.Keybind;
import org.quiltmc.loader.api.QuiltLoader;

import java.nio.file.Path;

public class Cosmiclook extends Omnilook {
	public final Keybind key;

	public Cosmiclook() {
		key = Keybind.fromDefaultKey(KEYBINDING_NAME, Input.Keys.GRAVE);
	}

	public Vector3 viewDir = new Vector3();

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		this.viewDir = InGame.getLocalPlayer().getEntity().viewDirection;
	}

	@Override
	public Path getConfigDir() {
		return QuiltLoader.getConfigDir();
	}

	@Override
	protected void setCameraType(int cameraType) {
		PlayerPerspective.setPlayerPerspective(cameraType == 0 ? PlayerPerspective.FIRST_PERSON : PlayerPerspective.THIRD_PERSON);
	}

	@Override
	protected int getCameraType() {
		return PlayerPerspective.getPlayerPerspective() == PlayerPerspective.FIRST_PERSON ? 0 : 1;
	}

	// ignored, uses viewDir above
	@Override
	protected float getMCXRot() {
		return 0;
	}

	@Override
	protected float getMCYRot() {
		return 0;
	}

	@Override
	protected boolean isKeyClicked() {
		return key.isJustPressed();
	}

	@Override
	protected boolean isKeyDown() {
		return key.isPressed();
	}
}
