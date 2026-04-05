package traben.entity_pin_cushions.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.Arrow;
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
        
        if (hitResult.getEntity() instanceof LivingEntity target) {
            if (projectile instanceof SpectralArrow && target instanceof Player player) {
                EntityPinCushions.addStuckSpectralArrowCount(player, 1);
            } else if (projectile instanceof Arrow && target instanceof Player player) {
                target.setArrowCount(target.getArrowCount() + 1);
            }
        }
    }
}