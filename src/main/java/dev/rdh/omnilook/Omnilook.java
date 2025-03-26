package dev.rdh.omnilook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Objects;

public abstract class Omnilook {
	public static final String ID = "omnilook";
	public static final Logger log = LogManager.getLogger(ID);

	protected static final String KEYBINDING_NAME = "key.omnilook.toggle";
	protected static final String KEYBINDING_CATEGORY = "key.categories.misc";

	// region instance
	private static Omnilook instance;

	public static Omnilook getInstance() {
		return Objects.requireNonNull(instance, "Omnilook has not been initialized");
	}

	protected Omnilook() {
		if(instance != null) {
			throw new IllegalStateException("Omnilook has already been initialized");
		}

		log.info("Omnilook initialized with " + getClass().getName());

		instance = this;

		Thread configThread = new Thread(Config::thread, "Omnilook Config Watcher");
		configThread.setDaemon(true);
		configThread.start();
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
		if(enabled) {
			lastCameraType = getCameraType();
			setCameraType(1);
			this.enabled = true;
		} else {
			setCameraType(lastCameraType);
			this.enabled = false;
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
	public abstract Path getConfigDir();

	protected abstract void setCameraType(int cameraType);

	protected abstract int getCameraType();

	protected abstract float getMCXRot();

	protected abstract float getMCYRot();

	protected abstract boolean isKeyClicked();

	protected abstract boolean isKeyDown();
	// endregion
}
