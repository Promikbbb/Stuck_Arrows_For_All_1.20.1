package traben.entity_pin_cushions.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_pin_cushions.EntityPinCushions;
import traben.entity_pin_cushions.ISpectralArrow;

@Mixin(Player.class)
public abstract class MixinPlayerEntity extends LivingEntity implements ISpectralArrow {

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void entityPinCushions$initDataTracker(CallbackInfo ci) {
        this.entityData.define(EntityPinCushions.STUCK_SPECTRAL_ARROW_COUNT, 0);
    }

    @Override
    public final int getStuckSpectralArrowCount() {
        return this.entityData.get(EntityPinCushions.STUCK_SPECTRAL_ARROW_COUNT);
    }

    @Override
    public final void setStuckSpectralArrowCount(int stuckArrowCount) {
        stuckArrowCount = Math.min(stuckArrowCount, EntityPinCushions.MAX_SPECTRAL_ARROWS);
        this.entityData.set(EntityPinCushions.STUCK_SPECTRAL_ARROW_COUNT, stuckArrowCount);
    }
    
    @Override
    public int getStuckSpectralArrowTimer() {
        return 0;
    }
    
    @Override
    public void setStuckSpectralArrowTimer(int timer) {
    }
}