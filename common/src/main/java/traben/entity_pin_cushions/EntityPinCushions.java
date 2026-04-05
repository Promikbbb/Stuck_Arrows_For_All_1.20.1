package traben.entity_pin_cushions;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityPinCushions {
    public static final String MOD_ID = "entity_pin_cushions";
    public static final int MAX_SPECTRAL_ARROWS = 10; 
    
    public static int PINCUSHION_ID = 0;
    public static int PINCUSHION_COUNT_ARROW = 0;
    public static int PINCUSHION_COUNT_STINGER = 0;
    
    private static final Map<UUID, Integer> SPECTRAL_ARROW_COUNTS = new HashMap<>();
    
    public static void init() {
    }
    
    public static void clearPlayerData(Player player) {
        if (player != null) {
            SPECTRAL_ARROW_COUNTS.remove(player.getUUID());
        }
    }
    
    public static int getStuckSpectralArrowCount(Player player) {
        if (player == null) return 0;
        return SPECTRAL_ARROW_COUNTS.getOrDefault(player.getUUID(), 0);
    }
    
    public static void setStuckSpectralArrowCount(Player player, int count) {
        if (player == null) return;
        count = Math.min(count, MAX_SPECTRAL_ARROWS);
        if (count <= 0) {
            SPECTRAL_ARROW_COUNTS.remove(player.getUUID());
        } else {
            SPECTRAL_ARROW_COUNTS.put(player.getUUID(), count);
        }
    }
    
    public static void addStuckSpectralArrowCount(Player player, int delta) {
        if (player == null) return;
        if (player.isCreative()) return;
        int current = getStuckSpectralArrowCount(player);
        setStuckSpectralArrowCount(player, current + delta);
    }
}