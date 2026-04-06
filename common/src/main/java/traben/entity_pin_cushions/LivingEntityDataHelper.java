package traben.entity_pin_cushions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class LivingEntityDataHelper {
    private static final ConcurrentHashMap<UUID, Integer> SPECTRAL_ARROW_COUNTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> SPECTRAL_ARROW_TIMERS = new ConcurrentHashMap<>();
    
    private static final String NBT_SPECTRAL_COUNT = "StuckSpectralArrowCount";
    private static final String NBT_SPECTRAL_TIMER = "StuckSpectralArrowTimer";
    
    public static int getStuckSpectralArrowCount(LivingEntity entity) {
        if (entity instanceof Player player && player instanceof ISpectralArrow spectralPlayer) {
            return spectralPlayer.getStuckSpectralArrowCount();
        }
        return SPECTRAL_ARROW_COUNTS.getOrDefault(entity.getUUID(), 0);
    }
    
    public static void setStuckSpectralArrowCount(LivingEntity entity, int count) {
        count = Math.min(count, EntityPinCushions.MAX_SPECTRAL_ARROWS);
        
        if (entity instanceof Player player && player instanceof ISpectralArrow spectralPlayer) {
            spectralPlayer.setStuckSpectralArrowCount(count);
        } else {
            if (count <= 0) {
                SPECTRAL_ARROW_COUNTS.remove(entity.getUUID());
            } else {
                SPECTRAL_ARROW_COUNTS.put(entity.getUUID(), count);
            }
        }
    }
    
    public static int getStuckSpectralArrowTimer(LivingEntity entity) {
        if (entity instanceof Player) {
            return EntityPinCushions.getStuckSpectralArrowTimer((Player) entity);
        }
        return SPECTRAL_ARROW_TIMERS.getOrDefault(entity.getUUID(), 0);
    }
    
    public static void setStuckSpectralArrowTimer(LivingEntity entity, int timer) {
        if (entity instanceof Player player) {
            EntityPinCushions.setStuckSpectralArrowTimer(player, timer);
        } else {
            if (timer <= 0) {
                SPECTRAL_ARROW_TIMERS.remove(entity.getUUID());
            } else {
                SPECTRAL_ARROW_TIMERS.put(entity.getUUID(), timer);
            }
        }
    }
    
    public static void saveToNBT(LivingEntity entity, CompoundTag tag) {
        if (entity instanceof Player) return;
        
        int count = SPECTRAL_ARROW_COUNTS.getOrDefault(entity.getUUID(), 0);
        int timer = SPECTRAL_ARROW_TIMERS.getOrDefault(entity.getUUID(), 0);
        
        if (count > 0) {
            tag.putInt(NBT_SPECTRAL_COUNT, count);
        }
        if (timer > 0) {
            tag.putInt(NBT_SPECTRAL_TIMER, timer);
        }
    }
    
    public static void loadFromNBT(LivingEntity entity, CompoundTag tag) {
        if (entity instanceof Player) return;
        
        if (tag.contains(NBT_SPECTRAL_COUNT)) {
            SPECTRAL_ARROW_COUNTS.put(entity.getUUID(), tag.getInt(NBT_SPECTRAL_COUNT));
        }
        if (tag.contains(NBT_SPECTRAL_TIMER)) {
            SPECTRAL_ARROW_TIMERS.put(entity.getUUID(), tag.getInt(NBT_SPECTRAL_TIMER));
        }
    }
    
    public static void removeData(LivingEntity entity) {
        if (!(entity instanceof Player)) {
            SPECTRAL_ARROW_COUNTS.remove(entity.getUUID());
            SPECTRAL_ARROW_TIMERS.remove(entity.getUUID());
        }
    }
}