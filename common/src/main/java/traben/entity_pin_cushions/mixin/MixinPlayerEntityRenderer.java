package traben.entity_pin_cushions.mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_pin_cushions.StuckSpectralArrowsFeatureRenderer;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    
    public MixinPlayerEntityRenderer(EntityRendererProvider.Context context, 
                                      PlayerModel<AbstractClientPlayer> model, 
                                      float shadowRadius) {
        super(context, model, shadowRadius);
    }
    
    @Inject(method = "<init>", at = @At("TAIL"))
    private void entityPinCushions$init(EntityRendererProvider.Context context, 
                                         boolean slim, 
                                         CallbackInfo ci) {
        addLayer(new StuckSpectralArrowsFeatureRenderer<>(context, this));
    }
}