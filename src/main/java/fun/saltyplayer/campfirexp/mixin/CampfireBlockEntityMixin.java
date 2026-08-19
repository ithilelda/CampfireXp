package fun.saltyplayer.campfirexp.mixin;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import fun.saltyplayer.campfirexp.Ownable;
import fun.saltyplayer.campfirexp.PlayerXpSavedData;


@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin implements Ownable {
    private UUID ownerUUID = null;
    private int xpTick = 20;

    public @Nullable Player getOwner(Level level) {
        if (ownerUUID == null) return null;
        return level.getPlayerByUUID(ownerUUID);
    }

    public void setOwner(Player player) {
        this.ownerUUID = player.getUUID();
    }

    public @Nullable UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Inject(method = "cookTick", at = @At("HEAD"))
    private void processXp(CampfireBlockEntity instance, ServerLevel level, BlockPos pos, BlockState state, CampfireBlockEntity blockEntity, RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipeCache, CallbackInfo ci) {
        if (ownerUUID != null) {
            xpTick--;
            instance.setChanged();
            if(xpTick <= 0) {
                AABB range = new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 1, pos.getZ() + 2);
                List<@NonNull ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, range);
                int totalXp = 0;
                for (ExperienceOrb orb : orbs) {
                    totalXp += orb.getValue();
                    orb.discard();
                }
                PlayerXpSavedData data = PlayerXpSavedData.get(level);
                data.addExp(ownerUUID, totalXp);
                // Reset the tick counter to a random value between 15 and 25 (20 ± 5) to avoid processing collisions.
                xpTick = 20 + level.getRandom().nextIntBetweenInclusive(-5,5);
            }
        }
    }
}
