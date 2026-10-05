package com.example.pocketportal.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Only the break time (hardness) is changed. Explosion resistance lives in a separate field
 * and is left alone, so obsidian still survives blasts like it always did.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
	/** Glass hardness. */
	private static final float GLASS_HARDNESS = 0.3F;

	@Shadow
	protected abstract BlockState asState();

	@Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
	private void pocketportal$glassLikeObsidian(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if (this.asState().is(Blocks.OBSIDIAN)) {
			cir.setReturnValue(GLASS_HARDNESS);
		}
	}
}
