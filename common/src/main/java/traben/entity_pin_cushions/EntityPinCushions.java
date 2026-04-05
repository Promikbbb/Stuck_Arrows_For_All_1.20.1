package traben.entity_pin_cushions;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityPinCushions {
    public static final String MOD_ID = "entity_pin_cushions";
    
    public static int PINCUSHION_ID = 0;
    public static int PINCUSHION_COUNT_ARROW = 0;
    public static int PINCUSHION_COUNT_STINGER = 0;
    
    private static final Map<UUID, Integer> SPECTRAL_ARROW_COUNTS = new HashMap<>();
    //private static final Map<UUID, Integer> CUSTOM_ARROW_COUNTS = new HashMap<>();
    
    public static void init() {
    }
    
    public static int getStuckSpectralArrowCount(Player player) {
        return SPECTRAL_ARROW_COUNTS.getOrDefault(player.getUUID(), 0);
    }
    
    public static void setStuckSpectralArrowCount(Player player, int count) {
        if (count <= 0) {
            SPECTRAL_ARROW_COUNTS.remove(player.getUUID());
        } else {
            SPECTRAL_ARROW_COUNTS.put(player.getUUID(), count);
        }
    }
    
    public static void addStuckSpectralArrowCount(Player player, int delta) {
        int current = getStuckSpectralArrowCount(player);
        setStuckSpectralArrowCount(player, current + delta);
    }
    
    public static void savePlayerData(CompoundTag tag, UUID playerId) {
        if (SPECTRAL_ARROW_COUNTS.containsKey(playerId)) {
            tag.putInt("spectralArrowCount", SPECTRAL_ARROW_COUNTS.get(playerId));
        }
    }
    
    public static void loadPlayerData(CompoundTag tag, UUID playerId) {
        if (tag.contains("spectralArrowCount")) {
            SPECTRAL_ARROW_COUNTS.put(playerId, tag.getInt("spectralArrowCount"));
        }
    }
}