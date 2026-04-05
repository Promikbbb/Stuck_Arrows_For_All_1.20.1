package traben.entity_pin_cushions.mixin;

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
        
        if (hitResult.getEntity() instanceof Player player) {
            if (projectile instanceof SpectralArrow && !player.isCreative()) {
                int currentCount = EntityPinCushions.getStuckSpectralArrowCount(player);
                if (currentCount < EntityPinCushions.MAX_SPECTRAL_ARROWS) {
                    EntityPinCushions.addStuckSpectralArrowCount(player, 1);
                    projectile.setNoGravity(true);
                    projectile.setDeltaMovement(0, 0, 0);
                }
            }
        }
    }
}