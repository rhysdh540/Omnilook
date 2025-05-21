package dev.rdh.omnilook;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.*;

import dev.rdh.omnilook.config.ModMenuScreenProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Optional;

public class LegacyFabriclook extends Omnilook implements ModMenuScreenProvider<GuiScreen> {
	public final KeyBinding key;
	private final MethodHandle getRenderViewEntity;

	public LegacyFabriclook() {
		KeyBinding key;
		try {
			key = new KeyBinding(KEYBINDING_NAME, Keyboard.KEY_GRAVE, KEYBINDING_CATEGORY);
		} catch (NoSuchMethodError e) {
			key = KeyBinding.class.getDeclaredConstructor(String.class, int.class).newInstance(KEYBINDING_NAME, Keyboard.KEY_GRAVE);
		}
		this.key = key;

		if(MixinPlugin.classExists("net.legacyfabric.fabric.api.client.keybinding.v1.KeyBindingHelper")) {
			net.legacyfabric.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(key);
		} else {
			Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer("minecraft");
			if(!mod.isPresent()) {
				throw new IllegalStateException("Minecraft mod not present?");
			}
			if(mod.get().getMetadata().getVersion().compareTo(Version.parse("1.7.0")) < 0) {
				throw new IllegalStateException("Omnilook requires Legacy Fabric API on 1.7.0+");
			}
		}

		Field f = MixinPlugin.field(Minecraft.class, "field_10309", "field_6279", "renderViewEntity");
		f.setAccessible(true);
		getRenderViewEntity = MethodHandles.lookup().unreflectGetter(f);
	}

	@Override
	public GuiScreen openScreen(GuiScreen parent) {
		StackTraceElement[] a = new Throwable().getStackTrace();
		for(StackTraceElement e : a) {
			if(e.getClassName().equals("io.github.prospector.modmenu.gui.ModsScreen") && e.getMethodName().equals("method_1044")) {
				return parent;
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
		Minecraft.getMinecraft().gameSettings.thirdPersonView = cameraType;
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

	public static MethodHandle getMH_setDisplayListEntitiesDirty() {
		try {
			MappingResolver mr = FabricLoader.getInstance().getMappingResolver();

			String setDisplayListEntitiesDirty = mr.mapMethodName(
					"intermediary",
					Type.getInternalName(RenderGlobal.class),
					"method_9917",
					"()V"
			);

			String renderGlobal = mr.mapFieldName(
					"intermediary",
					Type.getInternalName(Minecraft.class),
					"field_3804",
					Type.getDescriptor(RenderGlobal.class)
			);

			String getMinecraft = mr.mapMethodName(
					"intermediary",
					Type.getInternalName(Minecraft.class),
					"method_2965",
					Type.getMethodDescriptor(Type.getType(Minecraft.class))
			);

			return MethodHandles.collectArguments(
					MethodHandles.lookup().findVirtual(RenderGlobal.class, setDisplayListEntitiesDirty, MethodType.methodType(void.class)),
					0,
					MethodHandles.collectArguments(
							MethodHandles.lookup().findGetter(Minecraft.class, renderGlobal, RenderGlobal.class),
							0,
							MethodHandles.lookup().findStatic(Minecraft.class, getMinecraft, MethodType.methodType(Minecraft.class))
					)
			);
		} catch (Throwable e) {
			return MethodHandles.lookup().findStatic(MixinPlugin.class, "noop", MethodType.methodType(void.class));
		}
	}
}
