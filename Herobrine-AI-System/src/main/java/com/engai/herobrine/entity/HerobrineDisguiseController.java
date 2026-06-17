package com.engai.herobrine.entity;

import com.engai.herobrine.registry.ModEntities;
import com.engai.herobrine.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;

final class HerobrineDisguiseController {
	private static final double MIN_PROXIMITY_TRIGGER_DISTANCE = 8.0D;
	private static final double MAX_PROXIMITY_TRIGGER_DISTANCE = 15.0D;
	private static final int MAX_LIFETIME_TICKS = 20 * 150;
	private static final float PROXIMITY_REVEAL_CHANCE = 0.55F;

	private final Animal animal;
	private final boolean proximityRevealMode;
	private final double proximityTriggerDistance;
	private int lifetimeTicks = MAX_LIFETIME_TICKS;
	private boolean revealed;

	HerobrineDisguiseController(Animal animal) {
		this.animal = animal;
		this.proximityRevealMode = animal.getRandom().nextFloat() < PROXIMITY_REVEAL_CHANCE;
		this.proximityTriggerDistance = MIN_PROXIMITY_TRIGGER_DISTANCE
				+ animal.getRandom().nextDouble() * (MAX_PROXIMITY_TRIGGER_DISTANCE - MIN_PROXIMITY_TRIGGER_DISTANCE);
	}

	void tick(ServerLevel level) {
		if (this.revealed) {
			return;
		}
		if (--this.lifetimeTicks <= 0) {
			this.vanish(level);
			return;
		}

		ServerPlayer player = this.findNearestSurvivalPlayer(level, 48.0D);
		if (player == null) {
			this.animal.getNavigation().stop();
			return;
		}

		this.animal.setTarget(null);
		this.animal.getNavigation().stop();
		if (this.animal.distanceTo(player) <= 22.0D) {
			this.animal.getLookControl().setLookAt(player, 20.0F, 20.0F);
		}

		double distance = this.animal.distanceTo(player);
		if (this.proximityRevealMode && distance <= this.proximityTriggerDistance) {
			this.revealForAttack(level, player);
		}
	}

	boolean hurt(ServerLevel level, DamageSource source) {
		if (this.revealed) {
			return false;
		}
		if (source.getEntity() instanceof ServerPlayer player && this.isSurvivalPlayer(player)) {
			this.revealForCounterScare(level, player);
			return true;
		}
		return true;
	}

	private void revealForAttack(ServerLevel level, ServerPlayer player) {
		this.reveal(level, player, false);
	}

	private void revealForCounterScare(ServerLevel level, ServerPlayer player) {
		this.reveal(level, player, true);
	}

	private void reveal(ServerLevel level, ServerPlayer player, boolean counterScare) {
		if (this.revealed || !this.isSurvivalPlayer(player)) {
			return;
		}
		this.revealed = true;
		BlockPos revealPos = this.findRevealPosition(level, player);
		double x = revealPos.getX() + 0.5D;
		double y = revealPos.getY();
		double z = revealPos.getZ() + 0.5D;
		float yaw = this.getYawToward(player.position(), x, z);

		level.sendParticles(ParticleTypes.SMOKE, this.animal.getX(), this.animal.getY() + 0.7D, this.animal.getZ(), 38, 0.35D, 0.45D, 0.35D, 0.035D);
		level.playSound(null, this.animal.blockPosition(), ModSounds.CAVE_8, SoundSource.HOSTILE, 1.45F, 1.0F);
		this.animal.discard();

		HerobrineEntity herobrine = new HerobrineEntity(ModEntities.HEROBRINE, level);
		herobrine.snapTo(x, y, z, yaw, 0.0F);
		level.addFreshEntity(herobrine);
		level.sendParticles(ParticleTypes.SMOKE, x, y + 1.0D, z, 34, 0.35D, 0.65D, 0.35D, 0.03D);

		if (counterScare) {
			herobrine.setDisguiseCounterScare(player);
		} else {
			herobrine.setDisguiseAmbushAttack(player, 20);
		}
	}

	private void vanish(ServerLevel level) {
		this.revealed = true;
		level.sendParticles(ParticleTypes.SMOKE, this.animal.getX(), this.animal.getY() + 0.7D, this.animal.getZ(), 18, 0.25D, 0.35D, 0.25D, 0.025D);
		level.playSound(null, this.animal.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.55F, 0.75F);
		level.playSound(null, this.animal.blockPosition(), ModSounds.CAVE_2, SoundSource.HOSTILE, 1.1F, 1.0F);
		this.animal.discard();
	}

	private ServerPlayer findNearestSurvivalPlayer(ServerLevel level, double maxDistance) {
		ServerPlayer best = null;
		double bestDistance = maxDistance * maxDistance;
		for (ServerPlayer player : level.players()) {
			if (!this.isSurvivalPlayer(player)) {
				continue;
			}
			double distance = player.distanceToSqr(this.animal);
			if (distance < bestDistance) {
				bestDistance = distance;
				best = player;
			}
		}
		return best;
	}

	private BlockPos findRevealPosition(ServerLevel level, ServerPlayer player) {
		Vec3 awayFromPlayer = this.animal.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
		if (awayFromPlayer.lengthSqr() < 0.0001D) {
			awayFromPlayer = new Vec3(1.0D, 0.0D, 0.0D);
		}
		awayFromPlayer = awayFromPlayer.normalize();
		BlockPos preferred = BlockPos.containing(this.animal.position().add(awayFromPlayer.scale(0.5D)));
		for (BlockPos candidate : new BlockPos[]{preferred, this.animal.blockPosition(), preferred.above(), preferred.below()}) {
			if (this.canStandAt(level, candidate)) {
				return candidate;
			}
		}
		for (int radius = 1; radius <= 2; radius++) {
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos origin = this.animal.blockPosition().offset(x, 0, z);
					for (int y = 1; y >= -1; y--) {
						BlockPos candidate = origin.offset(0, y, 0);
						if (this.canStandAt(level, candidate)) {
							return candidate;
						}
					}
				}
			}
		}
		return this.animal.blockPosition();
	}

	private boolean canStandAt(ServerLevel level, BlockPos pos) {
		return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
				&& level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
				&& !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty();
	}

	private boolean isSurvivalPlayer(ServerPlayer player) {
		return player.isAlive() && !player.isDeadOrDying() && !player.isCreative() && !player.isSpectator();
	}

	private float getYawToward(Vec3 target, double x, double z) {
		double dx = target.x - x;
		double dz = target.z - z;
		return (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
	}
}
