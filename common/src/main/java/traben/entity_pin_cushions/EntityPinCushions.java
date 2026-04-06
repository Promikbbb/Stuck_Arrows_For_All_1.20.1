package traben.entity_pin_cushions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityPinCushions {
    public static final String MOD_ID = "entity_pin_cushions";
    public static final int MAX_SPECTRAL_ARROWS = 10;
    
    public static final EntityDataAccessor<Integer> STUCK_SPECTRAL_ARROW_COUNT = 
        SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
    
    public static int PINCUSHION_ID = 0;
    public static int PINCUSHION_COUNT_ARROW = 0;
    public static int PINCUSHION_COUNT_STINGER = 0;
    
    private static final Map<UUID, Integer> SPECTRAL_ARROW_TIMERS = new HashMap<>();
    
    public static void init() {
        System.out.println("EntityPinCushions initialized with DataTracker!");
    }
    
    public static void clearPlayerData(Player player) {
        if (player != null) {
            SPECTRAL_ARROW_TIMERS.remove(player.getUUID());
        }
    }
    
    public static int getStuckSpectralArrowCount(LivingEntity entity) {
        if (entity == null) return 0;
        if (entity instanceof Player player) {
            return getStuckSpectralArrowCount(player);
        }
        return LivingEntityDataHelper.getStuckSpectralArrowCount(entity);
    }
    
    public static void setStuckSpectralArrowCount(LivingEntity entity, int count) {
        if (entity == null) return;
        if (entity instanceof Player player) {
            setStuckSpectralArrowCount(player, count);
        } else {
            LivingEntityDataHelper.setStuckSpectralArrowCount(entity, count);
        }
    }
    
    public static void addStuckSpectralArrowCount(LivingEntity entity, int delta) {
        if (entity == null) return;
        if (entity instanceof Player player && player.isCreative()) return;
        int current = getStuckSpectralArrowCount(entity);
        setStuckSpectralArrowCount(entity, current + delta);
    }
    
    public static int getStuckSpectralArrowTimer(LivingEntity entity) {
        if (entity == null) return 0;
        if (entity instanceof Player player) {
            return getStuckSpectralArrowTimer(player);
        }
        return LivingEntityDataHelper.getStuckSpectralArrowTimer(entity);
    }
    
    public static void setStuckSpectralArrowTimer(LivingEntity entity, int timer) {
        if (entity == null) return;
        if (entity instanceof Player player) {
            setStuckSpectralArrowTimer(player, timer);
        } else {
            LivingEntityDataHelper.setStuckSpectralArrowTimer(entity, timer);
        }
    }
    
    public static int getStuckSpectralArrowCount(Player player) {
        if (player == null) return 0;
        if (player instanceof ISpectralArrow spectralPlayer) {
            return spectralPlayer.getStuckSpectralArrowCount();
        }
        return 0;
    }
    
    public static void setStuckSpectralArrowCount(Player player, int count) {
        if (player == null) return;
        if (player instanceof ISpectralArrow spectralPlayer) {
            spectralPlayer.setStuckSpectralArrowCount(count);
        }
    }
    
    public static void addStuckSpectralArrowCount(Player player, int delta) {
        if (player == null) return;
        if (player.isCreative()) return;
        int current = getStuckSpectralArrowCount(player);
        setStuckSpectralArrowCount(player, current + delta);
    }
    
    public static int getStuckSpectralArrowTimer(Player player) {
        if (player == null) return 0;
        return SPECTRAL_ARROW_TIMERS.getOrDefault(player.getUUID(), 0);
    }
    
    public static void setStuckSpectralArrowTimer(Player player, int timer) {
        if (player == null) return;
        if (timer <= 0) {
            SPECTRAL_ARROW_TIMERS.remove(player.getUUID());
        } else {
            SPECTRAL_ARROW_TIMERS.put(player.getUUID(), timer);
        }
    }
}