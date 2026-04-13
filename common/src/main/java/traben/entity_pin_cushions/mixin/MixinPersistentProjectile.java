package traben.entity_pin_cushions.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_pin_cushions.EntityPinCushions;

@Mixin(Projectile.class)
public class MixinPersistentProjectile {
    
    @Inject(
        method = "onHitEntity",
        at = @At("HEAD")
    )
    private void entityPinCushions$onHitEntity(EntityHitResult hitResult, CallbackInfo ci) {
        Projectile projectile = (Projectile) (Object) this;
        
        if (!projectile.isAlive()) {
            return;
        }
        
        if (hitResult.getEntity() instanceof LivingEntity living) {
            if (living instanceof Player player && player.isCreative()) {
                return;
            }
            
            if (projectile instanceof SpectralArrow spectralArrow) {
                if (living instanceof Player && spectralArrow.getOwner() instanceof Player) {
                    living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
                }
                
                int currentCount = EntityPinCushions.getStuckSpectralArrowCount(living);
                if (currentCount < EntityPinCushions.MAX_SPECTRAL_ARROWS) {
                    EntityPinCushions.addStuckSpectralArrowCount(living, 1);
                    projectile.setNoGravity(true);
                    projectile.setDeltaMovement(0, 0, 0);
                }
            }
        }
    }
}