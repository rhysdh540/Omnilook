package dev.rdh.omnilook;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.Type;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.Launch;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.file.Path;

public class Forgelook12 extends Omnilook {
	private final KeyBinding key;
	private static final MethodHandle getRenderViewEntity;
	public static final MethodHandle setDisplayListEntitiesDirty;

	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		Field f = MixinPlugin.field(Minecraft.class, "field_175622_Z", "field_71451_h", "renderViewEntity");
		f.setAccessible(true);
		getRenderViewEntity = lookup.unreflectGetter(f);
		MethodHandle s;
		try {
			String getMinecraftName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
					Type.getInternalName(Minecraft.class),
					"func_71410_x",
					Type.getMethodDescriptor(Type.getType(Minecraft.class))
			);
			MethodHandle getMinecraft = lookup.findStatic(Minecraft.class, getMinecraftName, MethodType.methodType(Minecraft.class));

			String setDisplayListEntitiesDirty = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
					Type.getInternalName(RenderGlobal.class),
					"func_174979_m",
					"()V"
			);

			String renderGlobal = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(
					Type.getInternalName(Minecraft.class),
					"field_71438_f",
					Type.getDescriptor(RenderGlobal.class)
			);

			s = MethodHandles.collectArguments(
					lookup.findVirtual(RenderGlobal.class, setDisplayListEntitiesDirty, MethodType.methodType(void.class)),
					0, MethodHandles.collectArguments(
							lookup.findGetter(Minecraft.class, renderGlobal, RenderGlobal.class),
							0, getMinecraft
					)
			);
		} catch (Throwable e) {
			OmniLog.info("using no-op");
			s = lookup.findStatic(MixinPlugin.class, "noop", MethodType.methodType(void.class));
		}

		setDisplayListEntitiesDirty = s;
	}

	public Forgelook12() {
		key = new KeyBinding(KEYBINDING_NAME, Keyboard.KEY_GRAVE, KEYBINDING_CATEGORY);

		Minecraft.getMinecraft().gameSettings.keyBindings = ArrayUtils.add(Minecraft.getMinecraft().gameSettings.keyBindings, key);
	}

	@Override
	public Path getConfigDir() {
		return Launch.minecraftHome.toPath().resolve("config");
	}

	@Override
	protected void setCameraType(int cameraType) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.gameSettings.thirdPersonView = cameraType;

		setDisplayListEntitiesDirty.invokeExact();
	}

	@Override
	protected int getCameraType() {
		return Minecraft.getMinecraft().gameSettings.thirdPersonView;
	}

	@Override
	protected float getMCXRot() {
		return ((Entity) getRenderViewEntity.invoke(Minecraft.getMinecraft())).rotationPitch;
	}

	@Override
	protected float getMCYRot() {
		return ((Entity) getRenderViewEntity.invoke(Minecraft.getMinecraft())).rotationYaw;
	}

	@Override
	protected boolean isKeyClicked() {
		return key.isPressed();
	}

	@Override
	protected boolean isKeyDown() {
		return key.isKeyDown();
	}
}
