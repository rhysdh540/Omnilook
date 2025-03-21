package dev.rdh.omnilook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public abstract class Omnilook {
	public static final String ID = "omnilook";
	public static final Logger log = LogManager.getLogger(ID);

	protected static final String KEYBINDING_NAME = "key.omnilook.toggle";

	// region instance
	private static Omnilook instance;

	public static Omnilook getInstance() {
		return Objects.requireNonNull(instance, "Omnilook has not been initialized");
	}

	protected Omnilook() {
		if (instance != null) {
			throw new IllegalStateException("Omnilook has already been initialized");
		}

		log.info("Omnilook initialized with {}", getClass().getName());

		instance = this;
	}
	// endregion

	// region logic
	private boolean enabled = false;
	private float yRot, xRot;

	// 0 - first person, 1 - third person back, 2 - third person front
	private int lastCameraType;

	/**
	 * Updates the freelook camera rotation.
	 * @param xRot the change in x rotation
	 * @param yRot the change in y rotation
	 * @return whether the regular minecraft camera should be updated
	 */
	public boolean update(float xRot, float yRot) {
		if (enabled) {
			xRot = this.xRot + xRot * 0.15F;
			if (xRot < -90.0F) {
				xRot = -90.0F;
			} else if (xRot > 90.0F) {
				xRot = 90.0F;
			}
			this.xRot = xRot;
			this.yRot += yRot * 0.15F;
			return false;
		}

		return true;
	}

	/**
	 * Toggles the freelook camera.
	 */
	public void toggle() {
		if (enabled) {
			setCameraType(lastCameraType);
			enabled = false;
		} else {
			lastCameraType = getCameraType();
			setCameraType(1);
			enabled = true;
		}

		this.xRot = getMCXRot();
		this.yRot = getMCYRot();
	}

	// endregion

	// region getters
	public boolean isEnabled() {
		return enabled;
	}

	public float getYRot() {
		return yRot;
	}

	public float getXRot() {
		return xRot;
	}
	// endregion

	// region impl
	protected abstract void setCameraType(int cameraType);
	protected abstract int getCameraType();
	protected abstract float getMCXRot();
	protected abstract float getMCYRot();
	// endregion
}
