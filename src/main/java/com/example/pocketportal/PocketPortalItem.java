package com.example.pocketportal;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Right-click to hop between the Overworld and the Nether at the usual 8:1 coordinates.
 * No portal blocks are placed in either dimension; you just land on the nearest safe spot.
 */
public class PocketPortalItem extends Item {
	private static final int COOLDOWN_TICKS = 40;
	private static final int SEARCH_RADIUS = 16;

	public PocketPortalItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(level instanceof ServerLevel from) || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}

		final double scale;
		final ResourceKey<Level> targetKey;
		if (from.dimension() == Level.OVERWORLD) {
			targetKey = Level.NETHER;
			scale = 1.0 / 8.0;
		} else if (from.dimension() == Level.NETHER) {
			targetKey = Level.OVERWORLD;
			scale = 8.0;
		} else {
			serverPlayer.displayClientMessage(Component.literal("The pocket portal only works in the Overworld and the Nether."), true);
			return InteractionResult.FAIL;
		}

		ServerLevel target = from.getServer().getLevel(targetKey);
		if (target == null) {
			serverPlayer.displayClientMessage(Component.literal("That dimension is not available."), true);
			return InteractionResult.FAIL;
		}

		double x = player.getX() * scale;
		double z = player.getZ() * scale;
		int preferredY = Math.clamp((int) Math.floor(player.getY()), target.getMinY() + 1, target.getMinY() + target.getLogicalHeight() - 8);

		Vec3 spot = findSafeSpot(target, BlockPos.containing(x, preferredY, z));
		if (spot == null) {
			serverPlayer.displayClientMessage(Component.literal("No safe landing spot found over there."), true);
			return InteractionResult.FAIL;
		}

		from.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS);
		serverPlayer.teleportTo(target, spot.x, spot.y, spot.z, Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot(), true);
		target.playSound(null, BlockPos.containing(spot), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS);

		ItemStack stack = player.getItemInHand(hand);
		player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
		return InteractionResult.SUCCESS;
	}

	/** Nearest standable position to {@code center}, searching outward in square rings. */
	private static Vec3 findSafeSpot(ServerLevel level, BlockPos center) {
		for (int r = 0; r <= SEARCH_RADIUS; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
						continue;
					}
					BlockPos column = center.offset(dx, 0, dz);
					if (!level.getWorldBorder().isWithinBounds(column)) {
						continue;
					}
					BlockPos feet = findSafeInColumn(level, column);
					if (feet != null) {
						return new Vec3(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
					}
				}
			}
		}
		return null;
	}

	/** Scans a column outward from the preferred height. */
	private static BlockPos findSafeInColumn(ServerLevel level, BlockPos start) {
		int minY = level.getMinY() + 1;
		int maxY = level.getMinY() + level.getLogicalHeight() - 2;
		int range = Math.max(start.getY() - minY, maxY - start.getY());
		for (int off = 0; off <= range; off++) {
			for (int sign = 1; sign >= -1; sign -= 2) {
				if (off == 0 && sign == -1) {
					continue;
				}
				int y = start.getY() + off * sign;
				if (y < minY || y > maxY) {
					continue;
				}
				BlockPos feet = new BlockPos(start.getX(), y, start.getZ());
				if (isSafe(level, feet)) {
					return feet;
				}
			}
		}
		return null;
	}

	private static boolean isSafe(ServerLevel level, BlockPos feet) {
		BlockPos head = feet.above();
		BlockPos floor = feet.below();
		BlockState floorState = level.getBlockState(floor);
		return level.getBlockState(feet).isAir()
			&& level.getBlockState(head).isAir()
			&& level.getFluidState(feet).isEmpty()
			&& floorState.isFaceSturdy(level, floor, Direction.UP)
			&& !floorState.is(Blocks.MAGMA_BLOCK)
			&& level.getFluidState(floor).isEmpty();
	}
}
