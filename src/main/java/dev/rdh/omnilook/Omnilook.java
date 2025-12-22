package dev.rdh.omnilook;

import dev.rdh.omnilook.config.Config;

import java.nio.file.Path;

public abstract class Omnilook {
	public static final String ID = "omnilook";
	public static final OmniLog LOGGER = OmniLog.get("Omnilook");

	protected static final String KEYBINDING_NAME = "key.omnilook.toggle";
	protected static final String KEYBINDING_CATEGORY = "key.categories.misc";

	// region instance
	private static Omnilook instance;

	public static Omnilook getInstance() {
		if (instance == null)
			throw new NullPointerException("Omnilook has not been initialized");
		return instance;
	}

	public static Omnilook getInstanceOrNull() {
		return instance;
	}

	protected Omnilook() {
		if(instance != null) {
			throw new IllegalStateException("Omnilook has already been initialized");
		}

		Omnilook.LOGGER.info("Omnilook initialized with {}", getClass().getName());

		instance = this;

		Config.init();
	}
	// endregion

	// region logic
	private boolean enabled = false;
	private float yRot, xRot;

	// 0 - first person, 1 - third person back, 2 - third person front
	private int lastCameraType;

	/**
	 * Updates the freelook camera rotation.
	 *
	 * @param xRot the change in rotation about the x axis (pitch)
	 * @param yRot the change in rotation about the y axis (yaw)
	 * @return whether the regular minecraft camera should be updated
	 */
	public boolean updateCamera(float xRot, float yRot) {
		if(!enabled) return true;

		xRot = this.xRot + xRot * 0.15F;
		if(xRot < -90.0F) {
			xRot = -90.0F;
		} else if(xRot > 90.0F) {
			xRot = 90.0F;
		}
		this.xRot = xRot;
		this.yRot += yRot * 0.15F;
		return false;
	}

	/**
	 * Updates the freelook camera state based on the keybind's state.
	 */
	public void update() {
		if(getCameraType() != 1 && enabled) {
			lastCameraType = getCameraType();
			setEnabled(false);
		}

		if(Config.toggleMode) {
			if(isKeyClicked()) {
				setEnabled(!enabled);
			}
		} else {
			boolean held = isKeyDown();
			if(held != enabled) {
				setEnabled(held);
			}
		}
	}

	/**
	 * Changes the freelook camera state.
	 * @param enabled the new state
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if(enabled) {
			lastCameraType = getCameraType();
			setCameraType(1);
		} else {
			setCameraType(lastCameraType);
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
	/** @return the directory where the config file is to be stored */
	public abstract Path getConfigDir();

	/**
	 * Sets the camera type.
	 * @param cameraType 0 - first person, 1 - third person back, 2 - third person front
	 */
	protected abstract void setCameraType(int cameraType);

	/**
	 * @return the current camera type
	 * @see #setCameraType
	 */
	protected abstract int getCameraType();

	/** @return the current rotation about the x axis (pitch) */
	protected abstract float getMCXRot();

	/** @return the current rotation about the y axis (yaw) */
	protected abstract float getMCYRot();

	/** @return whether the keybind was clicked (once) */
	protected abstract boolean isKeyClicked();

	/** @return whether the keybind is currently held down */
	protected abstract boolean isKeyDown();
	// endregion
}
