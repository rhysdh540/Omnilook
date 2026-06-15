package dev.rdh.omnilook.mixin.nil;

import nilloader.api.lib.asm.Type;
import nilloader.api.lib.mini.MiniTransformer;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import org.objectweb.asm.*;

import dev.rdh.omnilook.Omnilook;

/**
 * @see net.minecraft.client.render.Camera
 */
@Patch.Class("net.minecraft.client.render.Camera")
public class CameraHooks extends MiniTransformer {
	@Patch.Method("setup(Lnet/minecraft/entity/living/player/PlayerEntity;Z)V")
	public void patchSetup(PatchContext ctx) {
		PatchContext.SearchResult s = ctx.search(FSTORE(5));
		if (!s.isSuccessful()) {
			throw new IllegalStateException("Failed to find FSTORE 5 in Camera.setup");
		}

		s.jumpBefore();
		ctx.add(INVOKESTATIC(
				"dev/rdh/omnilook/mixin/nil/CameraHooks",
				"hookPitch",
				"(F)F"
		));

		s = ctx.search(FSTORE(6));
		if (!s.isSuccessful()) {
			throw new IllegalStateException("Failed to find FSTORE 6 in Camera.setup");
		}
		s.jumpBefore();
		ctx.add(INVOKESTATIC(
				Type.getInternalName(getClass()),
				"hookYaw",
				"(F)F"
		));
	}

	public static float hookPitch(float og) {
		Omnilook o = Omnilook.getInstance();
		o.update();
		return o.isEnabled() ? o.getXRot() : og;
	}

	public static float hookYaw(float og) {
		Omnilook o = Omnilook.getInstance();
		return o.isEnabled() ? o.getYRot() : og;
	}
}
