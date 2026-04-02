package traben.entity_pin_cushions.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_pin_cushions.EntityPinCushions;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {

    // Fix signature 1.20.1
    @Inject(method = "render", at = @At("HEAD"))
    private void render(
            Entity entity,
            double x,
            double y,
            double z,
            float yaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            CallbackInfo ci) {
        
        EntityPinCushions.PINCUSHION_ID = entity.getId();
        
        if (entity instanceof LivingEntity alive) {
            EntityPinCushions.PINCUSHION_COUNT_ARROW = alive.getArrowCount();
            EntityPinCushions.PINCUSHION_COUNT_STINGER = alive.getStingerCount();
        } else {
            EntityPinCushions.PINCUSHION_COUNT_ARROW = 0;
            EntityPinCushions.PINCUSHION_COUNT_STINGER = 0;
        }
    }
}