package traben.entity_pin_cushions.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_pin_cushions.EntityPinCushions;
import traben.entity_pin_cushions.ISpectralArrow;

@Mixin(Player.class)
public abstract class MixinPlayerEntity extends LivingEntity implements ISpectralArrow {
    
    @Unique
    private int entityPinCushions$stuckSpectralArrowCount = 0;
    
    @Unique
    private int entityPinCushions$stuckSpectralArrowTimer = 0;
    
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }
    
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void entityPinCushions$readSaveData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("entity_pin_cushions_spectral")) {
            entityPinCushions$stuckSpectralArrowCount = tag.getInt("entity_pin_cushions_spectral");
        }
    }
    
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void entityPinCushions$writeSaveData(CompoundTag tag, CallbackInfo ci) {
        if (entityPinCushions$stuckSpectralArrowCount > 0) {
            tag.putInt("entity_pin_cushions_spectral", entityPinCushions$stuckSpectralArrowCount);
        }
    }
    
    @Override
    public int getStuckSpectralArrowCount() {
        return entityPinCushions$stuckSpectralArrowCount;
    }
    
    @Override
    public void setStuckSpectralArrowCount(int count) {
        this.entityPinCushions$stuckSpectralArrowCount = count;
    }
    
    @Override
    public int getStuckSpectralArrowTimer() {
        return entityPinCushions$stuckSpectralArrowTimer;
    }
    
    @Override
    public void setStuckSpectralArrowTimer(int timer) {
        this.entityPinCushions$stuckSpectralArrowTimer = timer;
    }
}