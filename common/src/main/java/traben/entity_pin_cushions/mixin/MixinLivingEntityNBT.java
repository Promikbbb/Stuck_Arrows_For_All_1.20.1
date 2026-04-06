package traben.entity_pin_cushions.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_pin_cushions.LivingEntityDataHelper;

@Mixin(LivingEntity.class)
public class MixinLivingEntityNBT {
    
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void entityPinCushions$saveData(CompoundTag compound, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        LivingEntityDataHelper.saveToNBT(self, compound);
    }
    
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void entityPinCushions$loadData(CompoundTag compound, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        LivingEntityDataHelper.loadFromNBT(self, compound);
    }
    
    @Inject(method = "die", at = @At("HEAD"))
    private void entityPinCushions$onDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        LivingEntityDataHelper.removeData(self);
    }
}