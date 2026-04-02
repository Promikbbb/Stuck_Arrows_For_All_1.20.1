package traben.entity_pin_cushions.mixin;

import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_pin_cushions.PinCushionLayer;

@Mixin(value = LivingEntityRenderer.class, priority = 2000)
public abstract class MixinAddLayer<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow
    protected M model;

    @Shadow
    protected abstract boolean addLayer(RenderLayer<T, M> layer);

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void allStuckArrows$mixin(EntityRendererProvider.Context context, EntityModel<?> model, float shadowRadius, CallbackInfo ci) {
        if (this.model instanceof AgeableListModel || this.model instanceof HierarchicalModel) {
            // && !(this.model instanceof PlayerModel)
            try {
                @SuppressWarnings("unchecked")
                LivingEntityRenderer<T, M> self = (LivingEntityRenderer<T, M>) (Object) this;
                addLayer(new PinCushionLayer.ArrowLayer<>(context, self));
                addLayer(new PinCushionLayer.BeeStingerLayer<>(self));
            } catch (Exception e) {
                System.out.println("Failed to add custom stuck arrows layer to " + this.getClass().getName());
                e.printStackTrace();
            }
        }
    }
}
