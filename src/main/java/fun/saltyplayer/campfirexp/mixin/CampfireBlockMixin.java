package fun.saltyplayer.campfirexp.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fun.saltyplayer.campfirexp.Ownable;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin extends Block {

    public CampfireBlockMixin(Properties properties) {
		super(properties);
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
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof CampfireBlockEntity cbe) {
				cbe.setChanged();
				Ownable ownable = (Ownable) cbe;
				ownable.setOwner(player);
			}
		}
	}
}
