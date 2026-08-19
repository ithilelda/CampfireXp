package fun.saltyplayer.campfirexp.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;

import fun.saltyplayer.campfirexp.Ownable;
import fun.saltyplayer.campfirexp.PlayerXpSavedData;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin extends Block {

    public CampfireBlockMixin(Properties properties) {
		super(properties);
	}

	@ModifyExpressionValue(method = "useItemOn", at = @At(value = "INVOKE", target = "net/minecraft/world/item/crafting/RecipePropertySet.test(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean getXpWhenUse(boolean original, ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, @Local CampfireBlockEntity campfireBlockEntity, @Local(ordinal = 1) ItemStack itemInHand, @Cancellable CallbackInfoReturnable<InteractionResult> cir) {
		if (!original) {
            if (hand == InteractionHand.MAIN_HAND && level instanceof ServerLevel serverLevel) {
				PlayerXpSavedData xp_data = PlayerXpSavedData.get(serverLevel);
                if (player.isCrouching()) {
                    player.sendOverlayMessage(Component.literal("Currently stored " + xp_data.getExp(player.getUUID()) + " points of xp for player " + player.getName().getString()));
                    cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
                }
            }
        }
        return original;
	}

	// make every damage set the player hit timer, so that mobs drop xp orbs.
    @Inject(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void onCollisionSetHitTimer(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise, CallbackInfo ci) {
        LivingEntityAccessor accessor = (LivingEntityAccessor) entity;
		accessor.campfirexp$setLastHurtByPlayerMemoryTime(100);
    }

	@Override
	public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable LivingEntity by, @NonNull ItemStack itemStack) {
		super.setPlacedBy(level, pos, state, by, itemStack);
		if (!level.isClientSide() && by instanceof Player player) {
			Ownable ownable = (Ownable) level.getBlockEntity(pos);
			if (ownable != null) {
				ownable.setOwner(player);
			}
		}
	}
}
