package dev.rdh.omnilook;

import cpw.mods.modlauncher.api.INameMappingService.Domain;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.file.Path;

// forge switched runtime mappings in 1.16.5
public final class Forgelook16 extends Omnilook {
	private final KeyMapping key;
	private final MethodHandle[] cameraTypeHandles;
	private final MethodHandle getCamEntity;

	public Forgelook16() {
		key = new KeyMapping(KEYBINDING_NAME, GLFW.GLFW_KEY_GRAVE_ACCENT, KEYBINDING_CATEGORY);

		if(FMLEnvironment.dist != Dist.CLIENT) {
			OmniLog.error("Omnilook is a client-side mod and cannot be loaded on a server.");
			cameraTypeHandles = null;
			getCamEntity = null;
			return;
		}

		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodHandle g, s;
		if (MixinPlugin.classExists(ObfuscationReflectionHelper.remapName(Domain.CLASS, "net.minecraft.client.settings.PointOfView"))) {
			String getCameraTypeName = ObfuscationReflectionHelper.remapName(Domain.METHOD, "func_243230_g");
			String setCameraTypeName = ObfuscationReflectionHelper.remapName(Domain.METHOD, "func_243229_a");
			g = MethodHandles.filterArguments(
					lookup.findVirtual(CameraType.class, "ordinal", MethodType.methodType(int.class)), 0,
					lookup.findVirtual(Options.class, getCameraTypeName, MethodType.methodType(CameraType.class))
			);
			s = MethodHandles.filterArguments(
					lookup.findVirtual(Options.class, setCameraTypeName, MethodType.methodType(void.class, CameraType.class)), 1,
					MethodHandles.arrayElementGetter(CameraType[].class).bindTo(CameraType.values())
			);
		} else {
			String fieldName = ObfuscationReflectionHelper.remapName(Domain.FIELD, "field_74320_O");
			g = lookup.findGetter(Options.class, fieldName, int.class);
			s = lookup.findSetter(Options.class, fieldName, int.class);
		}

		cameraTypeHandles = new MethodHandle[] {g, s};

		Field cameraEntity = Minecraft.class.getDeclaredField(ObfuscationReflectionHelper.remapName(Domain.FIELD, "field_175622_Z"));
		cameraEntity.setAccessible(true);
		getCamEntity = lookup.unreflectGetter(cameraEntity);

		Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, key);
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
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
		return ((Entity) getCamEntity.invoke(Minecraft.getInstance())).xRot;
	}

	@Override
	protected float getMCYRot() {
		return ((Entity) getCamEntity.invoke(Minecraft.getInstance())).yRot;
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
