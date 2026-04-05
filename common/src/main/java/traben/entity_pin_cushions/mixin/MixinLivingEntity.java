package traben.entity_pin_cushions.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_pin_cushions.EntityPinCushions;
import traben.entity_pin_cushions.ISpectralArrow;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements ISpectralArrow {
    
    @Unique
    private int entityPinCushions$stuckSpectralArrowTimer = 0; 
    
    @Shadow
    public abstract int getArrowCount();
    
    @Shadow
    public abstract void setArrowCount(int count);
    
    @Override
    public int getStuckSpectralArrowCount() {
        if ((Object) this instanceof Player player) {
            return EntityPinCushions.getStuckSpectralArrowCount(player);
        }
        return 0;
    }
    
    @Override
    public void setStuckSpectralArrowCount(int count) {
        if ((Object) this instanceof Player player) {
            EntityPinCushions.setStuckSpectralArrowCount(player, count);
        }
    }
    
    @Override
    public int getStuckSpectralArrowTimer() {
        return entityPinCushions$stuckSpectralArrowTimer; 
    }
    
    @Override
    public void setStuckSpectralArrowTimer(int timer) {
        this.entityPinCushions$stuckSpectralArrowTimer = timer; 
    }
    
    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getArrowCount()I"
        )
    )
    private void entityPinCushions$tick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        
        if (self instanceof Player player) {
            int spectralCount = getStuckSpectralArrowCount();
            
            if (spectralCount > 0) {
                if (entityPinCushions$stuckSpectralArrowTimer <= 0) {
                    entityPinCushions$stuckSpectralArrowTimer = 20 * (30 - Math.min(spectralCount, 29));
                }
                
                entityPinCushions$stuckSpectralArrowTimer--;
                
                if (entityPinCushions$stuckSpectralArrowTimer <= 0) {
                    setStuckSpectralArrowCount(spectralCount - 1);
                }
            }
        }
    }
}