package fun.saltyplayer.campfirexp;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface Ownable {
    @Nullable Player getOwner(Level level);
    void setOwner(Player player);
    @Nullable UUID getOwnerUUID();
}
