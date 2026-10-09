package dev.rdh.omnilook;

import net.fabricmc.loader.api.FabricLoader;
import net.ornithemc.osl.keybinds.api.KeybindEvents;
import net.ornithemc.osl.keybinds.api.KeybindRegistry;
import org.lwjgl.input.Keyboard;

import dev.rdh.omnilook.config.ModMenuScreenProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.Entity;

import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Arrays;

public class Avianlook extends Omnilook implements ModMenuScreenProvider<Screen>, Runnable {
	private static final boolean OSL = FabricLoader.getInstance().isModLoaded("osl-keybinds");
	private static final int DEFAULT_KEY = MixinPlugin.classExists("org.lwjgl.glfw.GLFW") ? 96 : 41;
	private static Minecraft minecraft;
	private KeyBinding key;
	private boolean keyWasPressed;
	private MethodHandle keyCode;
	private final MethodHandle getPerspective, setPerspective;
	private final Field keyBindings = MixinPlugin.field(GameOptions.class, "keyBindings", "f_41636781");
	private final MethodHandle player = getter(Minecraft.class, "player", "f_28396371");
	private final MethodHandle camera = getter(Minecraft.class, "camera", "f_63700667", "player", "f_28396371");

	public Avianlook() {
		keyBindings.setAccessible(true);
		Field field = MixinPlugin.field(GameOptions.class, "perspective", "f_33865429");
		field.setAccessible(true);
		MethodHandles.Lookup L = MethodHandles.lookup();
		MethodHandle setPerspective = L.unreflectSetter(field), getPerspective = L.unreflectGetter(field);
		if (field.getType() == boolean.class) {
			MethodHandle cmp = MethodHandles.insertArguments(L.findStatic(Integer.class, "compareUnsigned",
					MethodType.methodType(int.class, int.class, int.class)), 1, 0);
			MethodHandle nonzero = MethodHandles.filterReturnValue(cmp, MethodHandles.arrayElementGetter(boolean[].class).bindTo(new boolean[]{false, true}));
			setPerspective = MethodHandles.filterArguments(setPerspective, 1, nonzero);

			getPerspective = MethodHandles.guardWithTest(
					getPerspective,
					MethodHandles.dropArguments(MethodHandles.constant(int.class, 1), 0, GameOptions.class),
					MethodHandles.dropArguments(MethodHandles.constant(int.class, 0), 0, GameOptions.class)
			);
		}
		this.setPerspective = setPerspective;
		this.getPerspective = getPerspective;
		if(OSL) {
			KeybindEvents.REGISTER_KEYBINDS.register(this);
		}
	}

	@Override
	public void run() {
		key = KeybindRegistry.register(KEYBINDING_NAME, DEFAULT_KEY, KEYBINDING_CATEGORY);
	}

	public static void initialize(Minecraft mc, GameOptions options) {
		minecraft = mc;
		if(OSL) return;
		Avianlook o = (Avianlook) getInstanceOrNull();
		if(o == null) o = new Avianlook();
		if(o.key == null) {
			try {
				o.key = new KeyBinding("Freelook", DEFAULT_KEY, KEYBINDING_CATEGORY);
			} catch (NoSuchMethodError e) {
				o.key = KeyBinding.class.getDeclaredConstructor(String.class, int.class).newInstance("Freelook", 41);
			}
		}
		o.keyBindings.set(options, Arrays.copyOf(options.keyBindings, options.keyBindings.length + 1));
		options.keyBindings[options.keyBindings.length - 1] = o.key;
	}

	private static MethodHandle getter(Class<?> type, String... names) {
		Field field = MixinPlugin.field(type, names);
		field.setAccessible(true);
		return MethodHandles.lookup().unreflectGetter(field);
	}

	@Override
	public Screen openScreen(Screen parent) {
		StackTraceElement[] a = new Throwable().getStackTrace();
		for(StackTraceElement e : a) {
			if(e.getClassName().equals("com.terraformersmc.modmenu.gui.ModsScreen") && e.getMethodName().equals("m_41805697")) {
				return parent;
			}
		}

		return ModMenuScreenProvider.super.openScreen(parent);
	}

	@Override
	public void update() {
		if(key != null && player.invoke(minecraft) != null) super.update();
	}

	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	protected void setCameraType(int cameraType) {
		setPerspective.invokeExact(minecraft.options, cameraType);
	}

	@Override
	protected int getCameraType() {
		return (int) getPerspective.invokeExact(minecraft.options);
	}

	@Override
	protected float getMCXRot() {
		return ((Entity) camera.invoke(minecraft)).pitch;
	}

	@Override
	protected float getMCYRot() {
		return ((Entity) camera.invoke(minecraft)).yaw;
	}

	@Override
	protected boolean isKeyClicked() {
		try {
			return key.consumeClick();
		} catch (NoSuchMethodError e) {
			boolean pressed = isKeyDown();
			boolean clicked = pressed && !keyWasPressed;
			keyWasPressed = pressed;
			return clicked;
		}
	}

	@Override
	protected boolean isKeyDown() {
		try {
			return key.isPressed();
		} catch (NoSuchMethodError e) {
			if(keyCode == null) keyCode = getter(KeyBinding.class, "keyCode", "f_57264828");
			Minecraft mc = minecraft;
			return mc.screen == null && mc.focused && Keyboard.isKeyDown(
					(int) keyCode.invokeExact(key));
		}
	}
}
