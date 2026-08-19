package fun.saltyplayer.campfirexp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class PlayerXpSavedData extends SavedData {
    private final Map<UUID, Integer> expMap;
    
    @SuppressWarnings("null")
    public static final @NonNull Codec<PlayerXpSavedData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.unboundedMap(UUIDUtil.CODEC, Codec.INT)
                .fieldOf("exp_map")
                .forGetter(PlayerXpSavedData::getExpMap)
        ).apply(instance, PlayerXpSavedData::new)
    );

    public PlayerXpSavedData(Map<UUID, Integer> expMap) {
        this.expMap = new HashMap<>(expMap);
    }

    public PlayerXpSavedData() {
        this.expMap = new HashMap<>();
    }

    // Getter used by forGetter
    public Map<UUID, Integer> getExpMap() {
        return this.expMap;
    }

    // Read EXP for a player by UUID
    public int getExp(UUID playerUuid) {
        return expMap.getOrDefault(playerUuid, 0);
    }

    // Set or consume EXP for a player
    public void setExp(UUID playerUuid, int amount) {
        expMap.put(playerUuid, amount);
        setDirty(); // CRITICAL
    }

    // Add EXP for any player (online or offline) by UUID
    public void addExp(UUID playerUuid, int amount) {
        int current = getExp(playerUuid);
        setExp(playerUuid, current + amount);
    }

    // Utility getter attached to Overworld (Global persistence across all dimensions)
    public static PlayerXpSavedData get(ServerLevel level) {
        // Overworld data storage ensures persistent storage regardless of which dimension the block is in
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
            new SavedDataType<PlayerXpSavedData>(
                Identifier.fromNamespaceAndPath(CampfireXp.MOD_ID, "global_data"),
                PlayerXpSavedData::new,
                PlayerXpSavedData.CODEC,
                DataFixTypes.PLAYER
            )
        );
    }
}
