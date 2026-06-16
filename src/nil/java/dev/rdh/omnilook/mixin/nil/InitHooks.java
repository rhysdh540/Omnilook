package dev.rdh.omnilook.mixin.nil;

import nilloader.api.ClassTransformer;
import nilloader.api.NilLogger;
import nilloader.api.lib.asm.Type;
import nilloader.api.lib.mini.MiniTransformer;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;

import dev.rdh.omnilook.NilLook;
import dev.rdh.omnilook.Omnilook;

import net.minecraft.client.options.KeyBinding;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * @see net.minecraft.client.options.GameOptions
 */
@Patch.Class("net.minecraft.client.options.GameOptions")
public class InitHooks extends MiniTransformer implements Runnable {
	@Override
	public void run() {
		System.getProperties().put("ol.log", (Function<String, NilLogger>) NilLogger::get);
		Omnilook.LOGGER.info("registering class transformers");
		ClassTransformer.register(this);
		ClassTransformer.register(new CameraHooks());
		ClassTransformer.register(new GameRendererHooks());
	}

	@Patch.Method("load()V")
	public void injectKey(PatchContext ctx) {
		String owner = "net/minecraft/client/options/GameOptions";
		String remappedOwner = remapType(owner);
		String fieldName = remapField(owner, "keyBindings", "[Lnet/minecraft/client/options/KeyBinding;");
		String fieldDesc = remapFieldDesc("[Lnet/minecraft/client/options/KeyBinding;");

		ctx.jumpToStart();
		ctx.add(
				ALOAD(0),
				DUP(),
				GETFIELD(remappedOwner, fieldName, fieldDesc),
				CHECKCAST("java/lang/Object"),
				INVOKESTATIC(Type.getInternalName(getClass()), "injectKeys", "(Ljava/lang/Object;)Ljava/lang/Object;"),
				CHECKCAST(fieldDesc),
				PUTFIELD(remappedOwner, fieldName, fieldDesc)
		);
	}

	// arguments aren't typed to avoid loading the class
	@SuppressWarnings({"ManualArrayToCollectionCopy", "UseBulkOperation"})
	public static Object injectKeys(Object o) {
		Omnilook ol = Omnilook.getInstanceOrNull();
		if (ol == null) {
			ol = new NilLook();
		}
		ArrayList<KeyBinding> kbs = new ArrayList<>();
		for (KeyBinding kb : (KeyBinding[]) o) {
			kbs.add(kb);
		}
		kbs.add(((NilLook) ol).key);
		return kbs.toArray(new KeyBinding[0]);
	}
}
