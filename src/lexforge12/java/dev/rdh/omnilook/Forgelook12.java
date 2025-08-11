package dev.rdh.omnilook;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.*;


import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.Launch;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.file.Path;

public class Forgelook12 extends Omnilook {
	private final KeyBinding key;
	private static final MethodHandle getCamera;
	public static final MethodHandle onViewChanged;
	public static final MethodHandle updateShader;

	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		Field f = MixinPlugin.field(Minecraft.class, "field_175622_Z", "field_71451_h", "camera");
		f.setAccessible(true);
		getCamera = lookup.unreflectGetter(f);
		MethodHandle s = null, l;
		try {
			String getInstanceName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
					Type.getInternalName(Minecraft.class),
					"func_71410_x",
					Type.getMethodDescriptor(Type.getType(Minecraft.class))
			);
			MethodHandle getInstance = lookup.findStatic(Minecraft.class, getInstanceName, MethodType.methodType(Minecraft.class));

			String setDisplayListEntitiesDirty = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
					Type.getInternalName(WorldRenderer.class),
					"func_174979_m",
					"()V"
			);

			String worldRenderer = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(
					Type.getInternalName(Minecraft.class),
					"field_71438_f",
					Type.getDescriptor(WorldRenderer.class)
			);

			s = MethodHandles.collectArguments(
					lookup.findVirtual(WorldRenderer.class, setDisplayListEntitiesDirty, MethodType.methodType(void.class)),
					0, MethodHandles.collectArguments(
							lookup.findGetter(Minecraft.class, worldRenderer, WorldRenderer.class),
							0, getInstance
					)
			);

			String entityRenderer = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(
					Type.getInternalName(Minecraft.class),
					"field_71460_t",
					Type.getDescriptor(EntityRenderer.class)
			);

			String loadEntityShader = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
					Type.getInternalName(EntityRenderer.class),
					"func_175066_a",
					Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Entity.class))
			);

			l = MethodHandles.collectArguments(
					lookup.findVirtual(EntityRenderer.class, loadEntityShader, MethodType.methodType(void.class, Entity.class)),
					0, MethodHandles.collectArguments(
							lookup.findGetter(Minecraft.class, entityRenderer, EntityRenderer.class),
							0, getInstance
					)
			);
		} catch (Throwable e) {
			Omnilook.LOGGER.debug("using no-op, because {}", e.getMessage());
			if(s == null)
				s = lookup.findStatic(MixinPlugin.class, "noop", MethodType.methodType(void.class));
			l = MethodHandles.dropArguments(
					lookup.findStatic(MixinPlugin.class, "noop", MethodType.methodType(void.class)),
					0, Entity.class
			);
		}

		onViewChanged = s;
		updateShader = l;
	}

	public Forgelook12() {
		key = new KeyBinding(KEYBINDING_NAME, Keyboard.KEY_GRAVE, KEYBINDING_CATEGORY);

		Minecraft.getInstance().options.keyBindings = ArrayUtils.add(Minecraft.getInstance().options.keyBindings, key);
	}

	@Override
	public Path getConfigDir() {
		return Launch.minecraftHome.toPath().resolve("config");
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft mc = Minecraft.getInstance();
		mc.options.perspective = cameraType;
		updateShader.invokeExact((Entity) (cameraType == 0 ? (Entity) getCamera.invoke(mc) : null));

		onViewChanged.invokeExact();
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getInstance().options.perspective;
	}

	@Override
	protected float getMCXRot() {
		return ((Entity) getCamera.invoke(Minecraft.getInstance())).pitch;
	}

	@Override
	protected float getMCYRot() {
		return ((Entity) getCamera.invoke(Minecraft.getInstance())).yaw;
	}

	@Override
	protected boolean isKeyClicked() {
		return key.consumeClick();
	}

	@Override
	protected boolean isKeyDown() {
		return key.isPressed();
	}
}
