package dev.rdh.omnilook.mixin.nil;

import nilloader.api.lib.asm.Type;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.MiniTransformer;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.PatchContext.SearchResult;
import nilloader.api.lib.mini.annotation.Patch;

import dev.rdh.omnilook.Omnilook;

/**
 * @see net.minecraft.client.render.GameRenderer
 */
@Patch.Class("net.minecraft.client.render.GameRenderer")
public class GameRendererHooks extends MiniTransformer {
	@Patch.Method("render(F)V")
	@Patch.Method.AffectsControlFlow
	public void patchRender(PatchContext ctx) {
		while (true) {
			String lcpe = "net/minecraft/client/entity/living/player/LocalClientPlayerEntity";
			SearchResult s = ctx.search(INVOKEVIRTUAL(
					remapType(lcpe),
					remapMethod(lcpe.replace('/', '.'), "updateLocalPlayerCamera", "(FF)V"),
					"(FF)V"
			));
			if (!s.isSuccessful()) break;
			s.jumpBefore();
			LabelNode skip = new LabelNode();
			LabelNode after = new LabelNode();
				ctx.add(
						DUP2(),
						INVOKESTATIC(
								"dev/rdh/omnilook/mixin/nil/GameRendererHooks",
								"shouldCallOriginalUpdateLocalPlayerCamera",
								"(FF)Z"
						),
						IFEQ(skip)
				);
			s.jumpAfter();
			ctx.add(GOTO(after), skip, POP2(), POP(), after);
		}
	}

	@Patch.Method("transformCamera(F)V")
	public void patchTransformCamera(PatchContext ctx) {
		String le = "net/minecraft/entity/living/LivingEntity";
		String rle = remapType(le);
		String owner = le.replace('/', '.');
		for(String axis : new String[]{"yaw", "pitch"}) {
			String capitalized = Character.toUpperCase(axis.charAt(0)) + axis.substring(1);

			for(String field : new String[] {
					remapField(owner, axis, "F"),
					remapField(owner, "prev" + capitalized, "F")
			}) {
				SearchResult s;
				while((s = ctx.search(GETFIELD(rle, field, "F"))).isSuccessful()) {
					s.jumpAfter();
					ctx.add(INVOKESTATIC(Type.getInternalName(getClass()), "modify" + capitalized, "(F)F"));
				}
				ctx.jumpToStart();
			}
		}
	}

	public static float modifyYaw(float value) {
		Omnilook o = Omnilook.getInstanceOrNull();
		if (o == null) return value;
		if (o.isEnabled()) {
			return o.getYRot();
		}
		return value;
	}

	public static float modifyPitch(float value) {
		Omnilook o = Omnilook.getInstanceOrNull();
		if (o == null) return value;
		if (o.isEnabled()) {
			return o.getXRot();
		}
		return value;
	}

	public static boolean shouldCallOriginalUpdateLocalPlayerCamera(float yawDelta, float pitchDelta) {
		Omnilook o = Omnilook.getInstanceOrNull();
		return o == null || o.updateCamera(-pitchDelta, yawDelta);
	}
}
