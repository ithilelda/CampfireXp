package fun.saltyplayer.campfirexp.mixin;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
    private static final int XP_TICK_INTERVAL = 20;
    private UUID ownerUUID = null;

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

    // make the signaling boolean always true for owned campfires to circumvent lithium optimization.
    @ModifyVariable(at = @At("STORE"), method = "cookTick", ordinal = 0)
    private static boolean modifyChanged(boolean original, ServerLevel level, BlockPos pos, BlockState state, CampfireBlockEntity blockEntity, RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipeCache) {
        Ownable ownable = (Ownable) blockEntity;
        if (ownable.getOwnerUUID() != null) {
            original = true;
        }
        return original;
    }

    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void processXp(ServerLevel level, BlockPos pos, BlockState state, CampfireBlockEntity blockEntity, RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipeCache, CallbackInfo ci) {
        Ownable ownable = (Ownable) blockEntity;
        UUID ownerUUID = ownable.getOwnerUUID();
        if (ownerUUID != null) {
            int positionOffset = Math.abs(pos.hashCode()) % XP_TICK_INTERVAL;
            if((level.getGameTime() + positionOffset) % XP_TICK_INTERVAL == 0) {
                AABB range = new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 1, pos.getZ() + 2);
                List<@NonNull ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, range);
                int totalXp = 0;
                for (ExperienceOrb orb : orbs) {
                    totalXp += orb.getValue();
                    orb.discard();
                }
                PlayerXpSavedData data = PlayerXpSavedData.get(level);
                data.addExp(ownerUUID, totalXp);
            }
        }
    }
}
