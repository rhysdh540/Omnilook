package dev.rdh.omnilook.mixin.ornithe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.rdh.omnilook.Omnilook;
import net.minecraft.client.ParticleManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;
import org.objectweb.asm.Opcodes;

@Mixin({GameRenderer.class, WorldRenderer.class, EntityRenderDispatcher.class, ParticleManager.class})
public class ViewRotationMixin {
	// indev–inf-20100625 in render
	// inf-20100630–19w09a in transformCamera
	// 14w30a–19w09a in getLookVector
	@ModifyExpressionValue(method = {"transformCamera", "m_30033001", "m_66460096", "setupRender", "m_72725110", "getLookVector", "m_88366935", "prepare", "m_62512441", "render(Lnet/minecraft/entity/Entity;F)V", "m_00221171"}, remap = false, require = 0, at = {
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_80130373:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;yaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_74900774:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;lastYaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_80130373:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/LivingEntity;yaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_74900774:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/LivingEntity;lastYaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_40996800;f_80130373:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/player/PlayerEntity;yaw:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_40996800;f_74900774:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/player/PlayerEntity;lastYaw:F")
	})
	private float yaw(float value) {
		Omnilook o = Omnilook.getInstance();
		return o.isEnabled() ? o.getYRot() : value;
	}

	@ModifyExpressionValue(method = {"transformCamera", "m_30033001", "m_66460096", "setupRender", "m_72725110", "getLookVector", "m_88366935", "prepare", "m_62512441", "render(Lnet/minecraft/entity/Entity;F)V", "m_00221171"}, remap = false, require = 0, at = {
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_03549461:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;pitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_45219594:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;lastPitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_03549461:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/LivingEntity;pitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_45219594:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/LivingEntity;lastPitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_40996800;f_03549461:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/player/PlayerEntity;pitch:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_40996800;f_45219594:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/entity/living/player/PlayerEntity;lastPitch:F")
	})
	private float pitch(float value) {
		Omnilook o = Omnilook.getInstance();
		return o.isEnabled() ? o.getXRot() : value;
	}

	// inf-20100627/29 renders the camera in m_23117156
	@ModifyExpressionValue(method = "m_23117156", remap = false, require = 0,
			slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_74900774:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER)), at = {
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_80130373:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_74900774:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_80130373:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_74900774:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_40996800;f_80130373:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_40996800;f_74900774:F")
	})
	private float renderYaw(float value) {
		return yaw(value);
	}

	@ModifyExpressionValue(method = "m_23117156", remap = false, require = 0,
			slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_74900774:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER)), at = {
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_03549461:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_42232651;f_45219594:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_03549461:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_74425583;f_45219594:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_40996800;f_03549461:F"),
			@At(value = "FIELD", target = "Lnet/minecraft/unmapped/C_40996800;f_45219594:F")
	})
	private float renderPitch(float value) {
		return pitch(value);
	}

	// 14w05a+
	@ModifyExpressionValue(method = {"render(Lnet/minecraft/entity/Entity;F)V", "m_00221171"}, remap = false, require = 0, at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/unmapped/C_42232651;m_95361306(F)Lnet/minecraft/unmapped/C_94584382;"),
			@At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getRotationVector(F)Lnet/minecraft/util/math/Vec3d;")
	})
	private Vec3d particleRotation(Vec3d value) {
		Omnilook o = Omnilook.getInstance();
		if(!o.isEnabled()) return value;
		double yaw = Math.toRadians(o.getYRot());
		double pitch = Math.toRadians(o.getXRot());
		return new Vec3d(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
	}

}
