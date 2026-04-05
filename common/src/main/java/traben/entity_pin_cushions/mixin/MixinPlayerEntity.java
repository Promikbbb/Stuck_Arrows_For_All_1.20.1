package traben.entity_pin_cushions.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
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
    
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void entityPinCushions$readSaveData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("entity_pin_cushions_spectral")) {
            EntityPinCushions.setStuckSpectralArrowCount((Player) (Object) this, tag.getInt("entity_pin_cushions_spectral"));
        }
    }
    
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void entityPinCushions$writeSaveData(CompoundTag tag, CallbackInfo ci) {
        int spectralCount = getStuckSpectralArrowCount();
        if (spectralCount > 0) {
            tag.putInt("entity_pin_cushions_spectral", spectralCount);
        }
    }
    
    @Inject(method = "die", at = @At("HEAD"))
    private void entityPinCushions$onDeath(DamageSource source, CallbackInfo ci) {
        EntityPinCushions.clearPlayerData((Player) (Object) this);
    }
    
    @Override
    public int getStuckSpectralArrowCount() {
        return EntityPinCushions.getStuckSpectralArrowCount((Player) (Object) this);
    }
    
    @Override
    public void setStuckSpectralArrowCount(int count) {
        EntityPinCushions.setStuckSpectralArrowCount((Player) (Object) this, count);
    }
    
    @Override
    public int getStuckSpectralArrowTimer() {
        return ((ISpectralArrow)(Object)this).getStuckSpectralArrowTimer();
    }
    
    @Override
    public void setStuckSpectralArrowTimer(int timer) {
        ((ISpectralArrow)(Object)this).setStuckSpectralArrowTimer(timer);
    }
}