package dev.rdh.omnilook;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.lwjgl.glfw.GLFW;

import dev.rdh.omnilook.config.FabricClothScreen;
import dev.rdh.omnilook.config.FabricYACLScreen;
import dev.rdh.omnilook.config.ModMenuScreenProvider;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.file.Path;

public final class IntermediaryFabriclook extends Omnilook implements ModMenuScreenProvider<Screen> {
	public final KeyMapping key;
	private final MethodHandle[] cameraTypeHandles;
	private final MethodHandle[] xyRotHandles;

	public IntermediaryFabriclook() {
		KeyMapping k;
		try {
			k = new KeyMapping(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KeyMapping.Category.MISC);
		} catch (Throwable t) {
			//noinspection JavaReflectionMemberAccess
			k = KeyMapping.class.getDeclaredConstructor(String.class, int.class, String.class)
					.newInstance(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KEYBINDING_CATEGORY);
		}
		key = k;

		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MappingResolver mr = FabricLoader.getInstance().getMappingResolver();

		// before 1.16.2-pre1, there was just an int field, after there's getter/setter with CameraType
		MethodHandle g, s;
		if (MixinPlugin.classExists(mr.mapClassName("intermediary", "net.minecraft.class_5498"))) {
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
		} else {
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
	public Screen openScreen(Screen parent) {
		for(StackTraceElement e : new Throwable().getStackTrace()) {
			if(e.getClassName().equals("com.terraformersmc.modmenu.gui.ModsScreen") && e.getMethodName().equals("method_25426")) {
				return parent;
			}
		}

		if(MixinPlugin.classExists("dev.isxander.yacl3.api.YetAnotherConfigLib")) {
			try {
				return FabricYACLScreen.make(parent);
			} catch (Throwable ignored) {
			}
		}

		if(MixinPlugin.classExists("me.shedaniel.clothconfig2.api.ConfigBuilder")) {
			try {
				return FabricClothScreen.make(parent);
			} catch (Throwable ignored) {
			}
		}

		return ModMenuScreenProvider.super.openScreen(parent);
	}

	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	protected void setCameraType(int cameraType) {
		cameraTypeHandles[1].invokeExact(Minecraft.getInstance().options, cameraType);
		Minecraft.getInstance().gameRenderer.checkEntityPostEffect(cameraType == 0 ? Minecraft.getInstance().getCameraEntity() : null);
	}

	@Override
	protected int getCameraType() {
		return (int) cameraTypeHandles[0].invokeExact(Minecraft.getInstance().options);
	}

	@Override
	protected float getMCXRot() {
		return (float) xyRotHandles[0].invokeExact(Minecraft.getInstance().getCameraEntity());
	}

	@Override
	protected float getMCYRot() {
		return (float) xyRotHandles[1].invokeExact(Minecraft.getInstance().getCameraEntity());
	}

	@Override
	protected boolean isKeyClicked() {
		return key.consumeClick();
	}

	@Override
	protected boolean isKeyDown() {
		return key.isDown();
	}
}
