package dev.rdh.omnilook;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

public final class Modernlook extends Omnilook implements ClientModInitializer {
	public final KeyMapping key;
	private final MethodHandle[] cameraTypeHandles;
	private final MethodHandle[] xyRotHandles;

	public Modernlook() {
		key = new KeyMapping(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KeyMapping.CATEGORY_MISC);

		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MappingResolver mr = FabricLoader.getInstance().getMappingResolver();

		// before 1.16.2-pre1, there was just an int field, after there's getter/setter with CameraType
		MethodHandle g, s;
		try {
			String getCameraTypeName = mr.mapMethodName("intermediary", "net.minecraft.class_315", "method_31044", "()Lnet/minecraft/class_5498;");
			String setCameraTypeName = mr.mapMethodName("intermediary", "net.minecraft.class_315", "method_31043", "(Lnet/minecraft/class_5498;)V");
			g = MethodHandles.filterArguments(
					lookup.findVirtual(CameraType.class, "ordinal", MethodType.methodType(int.class)), 0,
					lookup.findVirtual(Options.class, getCameraTypeName, MethodType.methodType(CameraType.class))
			);
			s = MethodHandles.filterArguments(
					lookup.findVirtual(Options.class, setCameraTypeName, MethodType.methodType(void.class, CameraType.class)), 1,
					MethodHandles.arrayElementGetter(CameraType[].class).bindTo(CameraType.values())
			);
		} catch (NoClassDefFoundError t) {
			String fieldName = mr.mapFieldName("intermediary", "net.minecraft.class_315", "field_1850", "Lnet/minecraft/class_5498;");
			g = lookup.findGetter(Options.class, fieldName, int.class);
			s = lookup.findSetter(Options.class, fieldName, int.class);
		}

		cameraTypeHandles = new MethodHandle[] {g, s};

		// before 21w17a, the fields were public
		// since we're using reflection we can just access them directly anyway without needing to worry about the getters
		// use old reflection because private access is annoying and setAccessible is easy
		Field xRotField = Entity.class.getDeclaredField(mr.mapFieldName("intermediary", "net.minecraft.class_1297", "field_5965", "F"));
		Field yRotField = Entity.class.getDeclaredField(mr.mapFieldName("intermediary", "net.minecraft.class_1297", "field_6031", "F"));
		xRotField.setAccessible(true);
		yRotField.setAccessible(true);
		xyRotHandles = new MethodHandle[] {
				lookup.unreflectGetter(xRotField),
				lookup.unreflectGetter(yRotField)
		};
	}

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(key);
	}

	@Override
	protected void setCameraType(int cameraType) {
		cameraTypeHandles[1].invokeExact(Minecraft.getInstance().options, cameraType);
	}

	@Override
	protected int getCameraType() {
		return (int) cameraTypeHandles[0].invokeExact(Minecraft.getInstance().options);
	}

	@Override
	protected float getMCXRot() {
		return (float) xyRotHandles[0].invokeExact(Minecraft.getInstance().cameraEntity);
	}

	@Override
	protected float getMCYRot() {
		return (float) xyRotHandles[1].invokeExact(Minecraft.getInstance().cameraEntity);
	}
}
