package traben.entity_pin_cushions.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_pin_cushions.ISpectralArrow;
import traben.entity_pin_cushions.LivingEntityDataHelper;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements ISpectralArrow {
    
    @Shadow
    public abstract int getArrowCount();
    
    @Shadow
    public abstract void setArrowCount(int count);
    
    @Override
    public int getStuckSpectralArrowCount() {
        LivingEntity self = (LivingEntity) (Object) this;
        return LivingEntityDataHelper.getStuckSpectralArrowCount(self);
    }
    
    @Override
    public void setStuckSpectralArrowCount(int count) {
        LivingEntity self = (LivingEntity) (Object) this;
        LivingEntityDataHelper.setStuckSpectralArrowCount(self, count);
    }
    
    @Override
    public int getStuckSpectralArrowTimer() {
        LivingEntity self = (LivingEntity) (Object) this;
        return LivingEntityDataHelper.getStuckSpectralArrowTimer(self);
    }
    
    @Override
    public void setStuckSpectralArrowTimer(int timer) {
        LivingEntity self = (LivingEntity) (Object) this;
        LivingEntityDataHelper.setStuckSpectralArrowTimer(self, timer);
    }
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void entityPinCushions$tick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        
        int spectralCount = getStuckSpectralArrowCount();
        
        if (spectralCount > 0) {
            int timer = getStuckSpectralArrowTimer();
            if (timer <= 0) {
                timer = 20 * (30 - Math.min(spectralCount, 29));
                setStuckSpectralArrowTimer(timer);
            }
            
            timer--;
            setStuckSpectralArrowTimer(timer);
            
            if (timer <= 0) {
                setStuckSpectralArrowCount(spectralCount - 1);
            }
        } else {
            setStuckSpectralArrowTimer(0);
        }
    }
    
    @Inject(method = "die", at = @At("HEAD"))
    private void entityPinCushions$onDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        LivingEntityDataHelper.removeData(self);
    }
}