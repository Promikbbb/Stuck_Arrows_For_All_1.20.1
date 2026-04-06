package traben.entity_pin_cushions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SpectralArrow;
import traben.entity_pin_cushions.PinCushionLayer;

public class StuckSpectralArrowsFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> 
        extends PinCushionLayer<T, M> {
    
    private final EntityRenderDispatcher dispatcher;
    
    public StuckSpectralArrowsFeatureRenderer(EntityRendererProvider.Context context, 
                                               LivingEntityRenderer<T, M> renderer) {
        super(renderer);
        this.dispatcher = context.getEntityRenderDispatcher();
    }
    
    @Override
    protected int numStuck(T entity) {
    return LivingEntityDataHelper.getStuckSpectralArrowCount(entity);
    }
    
    @Override
    protected void renderStuckItem(PoseStack poseStack, MultiBufferSource buffer, 
                                   int packedLight, Entity entity, 
                                   float x, float y, float z, float partialTick) {
        float f = Mth.sqrt(x * x + z * z);
        SpectralArrow spectralArrow = new SpectralArrow(entity.level(), entity.getX(), entity.getY(), entity.getZ());
        spectralArrow.setYRot((float) (Math.atan2(x, z) * 57.2957763671875));
        spectralArrow.setXRot((float) (Math.atan2(y, f) * 57.2957763671875));
        spectralArrow.yRotO = spectralArrow.getYRot();
        spectralArrow.xRotO = spectralArrow.getXRot();
        
        spectralArrow.tickCount = -9999;
        
        this.dispatcher.render(spectralArrow, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, buffer, packedLight);
    }
}