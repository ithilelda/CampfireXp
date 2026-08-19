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
				if (itemStack.isEmpty()) {
					// crouching with an empty hand will deposit exactly one level of experience to the campfire.
					if (player.isCrouching()) {
						if (player.experienceLevel > 0) {
							player.experienceLevel--;
							int xp_needed = player.getXpNeededForNextLevel();
							player.experienceLevel++;
							player.giveExperiencePoints(-xp_needed);
							xp_data.addExp(player.getUUID(), xp_needed);
							cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
						} else {
							player.giveExperienceLevels(-player.totalExperience);
							xp_data.addExp(player.getUUID(), player.totalExperience);
							cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
						}
					// standing with an empty hand will withdraw exactly one level of experience from the campfire.
                	} else {
						int xp_needed = player.getXpNeededForNextLevel();
						int current_xp = xp_data.getExp(player.getUUID());
						int actual_xp = Math.min(xp_needed, current_xp);
						player.giveExperiencePoints(actual_xp);
						xp_data.addExp(player.getUUID(), -actual_xp);
						cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
					}
				}
                
				// if the interaction is successful, send an overlay message to indicate the current xp storage amount for player.
				if (cir.getReturnValue() == InteractionResult.SUCCESS_SERVER) {
					player.sendOverlayMessage(Component.literal("Currently stored " + xp_data.getExp(player.getUUID()) + " points of xp for player " + player.getName().getString()));
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
