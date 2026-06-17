package com.engai.herobrine.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import com.engai.herobrine.network.HerobrineVisualEffectPayload;
import com.engai.herobrine.registry.ModSounds;
import com.engai.herobrine.world.HerobrineSpawnManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HerobrineEntity extends Monster {
	public static final float MAX_HEALTH_POINTS = 4000.0F;
	public static final float VANISH_HEALTH_POINTS = 2800.0F;
	private static final float VANILLA_HEALTH_POINTS = 100.0F;
	private static final double GIVE_UP_DISTANCE = 60.0D;
	private static final double STALKING_MAX_DISTANCE = 320.0D;
	private static final float LIGHTNING_HEALTH_STEP_POINTS = 100.0F;
	private static final int ARMOR_DROP_HIT_COUNT = 4;
	private static final int ATTACK_COOLDOWN_TICKS = 18;
	private static final int DEFENSIVE_RETREAT_WINDOW_TICKS = 20 * 12;
	private static final int STUCK_DOOR_TICKS = 8;
	private static final int STUCK_BREAK_TICKS = 18;
	private static final int STUCK_TNT_TICKS = 70;
	private static final int STUCK_TELEPORT_TICKS = 150;
	private static final int OBSTACLE_ACTION_COOLDOWN_TICKS = 10;
	private static final int TELEPORT_ACTION_COOLDOWN_TICKS = 20 * 10;
	private static final int WEAPON_REROLL_TICKS = 600;
	private static final int POST_KILL_COOLDOWN_TICKS = 500;
	private static final double POST_KILL_RETREAT_MIN_DISTANCE = 45.0D;
	private static final double POST_KILL_RETREAT_EXTRA_DISTANCE = 25.0D;
	private static final double BUILD_HORIZONTAL_RANGE = 5.5D;
	private static final double BUILD_MIN_VERTICAL_GAP = 2.25D;
	private static final int BUILD_ACTION_COOLDOWN_TICKS = 20 * 8;
	private static final int CLIMB_ACTION_COOLDOWN_TICKS = 6;
	private static final int CLIMB_SCAN_RADIUS = 6;
	private static final int CLIMB_SCAN_UP = 14;
	private static final double CLIMB_REACH_DISTANCE_SQR = 2.75D;
	private static final int CLIMB_COMMIT_TICKS = 160;
	private static final int VERTICAL_ROUTE_SCAN_RADIUS = 7;
	private static final int VERTICAL_ROUTE_SCAN_UP = 9;
	private static final int VERTICAL_ROUTE_COMMIT_TICKS = 60;
	private static final double VERTICAL_ROUTE_REACH_DISTANCE_SQR = 3.25D;
	private static final int SMART_PLAN_SCAN_RADIUS = 28;
	private static final int SMART_PLAN_SCAN_UP = 24;
	private static final int SMART_PLAN_SCAN_DOWN = 32;
	private static final int SMART_PLAN_COOLDOWN_TICKS = 20;
	private static final int DOOR_PLAN_COMMIT_TICKS = 140;
	private static final double DOOR_PLAN_REACH_DISTANCE_SQR = 5.0D;
	private static final int DOOR_PLAN_NO_PROGRESS_TICKS = 45;
	private static final int REJECTED_DOOR_COOLDOWN_TICKS = 500;
	private static final double DOOR_PROGRESS_EPSILON = 0.75D;
	private static final int BUILD_COLUMN_COMMIT_TICKS = 20 * 12;
	private static final int BUILD_COLUMN_SCAN_RADIUS = 5;
	private static final int BUILD_COLUMN_MAX_BLOCKS = 3;
	private static final int BUILD_COLUMN_PLACE_DELAY_TICKS = 20 * 2;
	private static final double BUILD_COLUMN_REACH_DISTANCE_SQR = 2.8D;
	private static final double DIRECT_BREACH_HORIZONTAL_RANGE = 8.0D;
	private static final double DIRECT_BREACH_VERTICAL_GAP = 2.75D;
	private static final double DIRECT_BREACH_REACH_DISTANCE_SQR = 22.0D;
	private static final int DIRECT_BREACH_TNT_AFTER_TICKS = 120;
	private static final int DIRECT_BREACH_TNT_COOLDOWN_TICKS = 220;
	private static final int DIRECT_BREACH_TNT_IGNITE_DELAY_TICKS = 18;
	private static final float BOMB_HEALTH_STEP_POINTS = 200.0F;
	private static final float BOMB_EXPLOSION_POWER = 0.85F;
	private static final float REVENGE_FIRST_STEP_POINTS = 40.0F;
	private static final float REVENGE_SECOND_STEP_POINTS = 80.0F;
	private static final float REVENGE_REPEAT_STEP_POINTS = 120.0F;
	private static final int REVENGE_TOTAL_TICKS = 94;
	private static final int REVENGE_KNOCKBACK_TICK = 2;
	private static final int REVENGE_TELEPORT_TICK = 18;
	private static final int REVENGE_FIREBALL_TICK = 44;
	private static final int REVENGE_ARMOR_DROP_TICK = 52;
	private static final int REVENGE_LIGHTNING_TICK = 68;
	private static final double REVENGE_TELEPORT_DISTANCE = 7.0D;
	private static final double REVENGE_KNOCKBACK_POWER = 3.2D;
	private static final double REVENGE_STUN_HORIZONTAL_LIMIT = 0.06D;
	private static final float REVENGE_LIGHTNING_GROUND_POWER = 1.15F;
	private static final double DEFENSIVE_RETREAT_MIN_DISTANCE = 30.0D;
	private static final double DEFENSIVE_RETREAT_EXTRA_DISTANCE = 5.0D;
	private static final int THEFT_TASK_MAX_STOLEN_SLOTS = 7;
	private static final int THEFT_DOOR_SCAN_RADIUS = 9;
	private static final double THEFT_DOOR_REACH_DISTANCE_SQR = 4.0D;
	private static final int DEATH_LOOT_SCENE_TICKS = 20 * 6;
	private static final int DEATH_LOOT_FIRST_PICKUP_TICKS = 14;
	private static final int DEATH_LOOT_PICKUP_INTERVAL_TICKS = 10;
	private static final int STALKING_ROAM_MIN_COOLDOWN_TICKS = 20 * 4;
	private static final int STALKING_ROAM_RANDOM_COOLDOWN_TICKS = 20 * 5;
	public static final double ATTACK_RANGE = 1.80D;
	private boolean healthInitialized;
	private float mythicHealth = MAX_HEALTH_POINTS;
	private float nextBombHealthThreshold = MAX_HEALTH_POINTS - BOMB_HEALTH_STEP_POINTS;
	private float nextLightningHealthThreshold = MAX_HEALTH_POINTS - LIGHTNING_HEALTH_STEP_POINTS;
	private UUID combatTargetId;
	private int lightningStrikes;
	private int lightningCooldown;
	private int lightningWindowTicks;
	private int consecutiveHandHits;
	private int attackCooldown;
	private int stuckTicks;
	private int obstacleActionCooldown;
	private int pacifyNearbyMobsCooldown;
	private int smartPlanCooldown;
	private int rejectedDoorPlanCooldown;
	private int directBreachTicks;
	private int directBreachTntCooldown;
	private BlockPos pendingBreachTntPos;
	private int pendingBreachTntTicks;
	private int weaponRerollTicks;
	private int postKillCooldownTicks;
	private BlockPos breakingBlockPos;
	private int breakingTicks;
	private int breakingTotalTicks;
	private BlockPos activeClimbPos;
	private int activeClimbTicks;
	private BlockPos activeVerticalRoutePos;
	private int activeVerticalRouteTicks;
	private BlockPos activeDoorPlanPos;
	private int activeDoorPlanTicks;
	private double activeDoorPlanLastDistanceSqr;
	private int activeDoorPlanNoProgressTicks;
	private BlockPos rejectedDoorPlanPos;
	private BlockPos activeBuildColumnPos;
	private int activeBuildColumnTicks;
	private int activeBuildColumnBlocksPlaced;
	private int activeBuildColumnPlaceDelay;
	private BlockPos tunnelStartPos;
	private BlockPos tunnelCurrentBlockPos;
	private Direction tunnelDirection;
	private int tunnelLength;
	private int tunnelBlockIndex;
	private int tunnelBreakingTicks;
	private int tunnelBreakingTotalTicks;
	private List<BlockPos> taskBreakPositions = List.of();
	private List<BlockPlacement> taskPlacements = List.of();
	private BlockPos taskBreakingBlockPos;
	private int taskBreakIndex;
	private int taskPlaceIndex;
	private int taskBreakingTicks;
	private int taskBreakingTotalTicks;
	private int taskPlaceCooldown;
	private boolean theftTaskMode;
	private BlockPos theftTargetPos;
	private BlockPos theftDoorPos;
	private BlockPos theftWanderPos;
	private int theftTaskTicks;
	private int theftTaskPhase;
	private BlockPos stalkingWatchPos;
	private BlockPos stalkingRoamTarget;
	private int stalkingTravelTicks;
	private int stalkingRoamCooldown;
	private int stalkingCoverStep;
	private int stalkingCoverCooldown;
	private float aggressiveStalkingAttackChance;
	private int stalkingTicks;
	private boolean usingUtilityTool;
	private boolean attackEnabled = true;
	private boolean tunnelDiggingMode;
	private boolean blockTaskMode;
	private boolean stalkingVanishCue;
	private boolean stalkingSettled;
	private boolean aggressiveStalking;
	private boolean stalkingReactionUsed;
	private boolean defensiveRetreatUsed;
	private boolean playerWasHoldingSword;
	private boolean defensiveSwordCheckResolved;
	private int combatTicks;
	private UUID disguiseAmbushTargetId;
	private int disguiseAmbushDelayTicks;
	private UUID disguiseCounterTargetId;
	private int disguiseCounterTicks;
	private boolean disguiseCounterHitDone;
	private float revengeDamageProgress;
	private int revengeStepIndex;
	private int pendingRevengeStage;
	private int revengeComboTicks;
	private int revengeComboStage;
	private UUID revengeTargetId;
	private boolean revengeKnockbackDone;
	private boolean revengeTeleportDone;
	private boolean revengeFireballDone;
	private boolean revengeArmorDropDone;
	private boolean revengeLightningDone;
	private UUID deathLootTargetId;
	private BlockPos deathLootPos;
	private int deathLootTicks;
	private int deathLootPickupCooldown;
	private double lastCombatX;
	private double lastCombatZ;

	public record BlockPlacement(BlockPos pos, BlockState state) {
	}

	public HerobrineEntity(EntityType<? extends HerobrineEntity> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 0;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, VANILLA_HEALTH_POINTS)
				.add(Attributes.ATTACK_DAMAGE, 4.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.29D)
				.add(Attributes.FOLLOW_RANGE, 80.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
	}

	@Override
	public boolean canBeSeenAsEnemy() {
		return false;
	}

	private void tickNonPlayerAggroCleanup(ServerLevel serverLevel) {
		if (this.pacifyNearbyMobsCooldown > 0) {
			this.pacifyNearbyMobsCooldown--;
			return;
		}
		this.pacifyNearbyMobsCooldown = 20;
		for (Mob mob : serverLevel.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(56.0D), mob -> mob != this && mob.getTarget() == this)) {
			mob.setTarget(null);
		}
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new HerobrineChaseGoal(this));
		this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.85D));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.knockbackCooldown > 0) {
			this.knockbackCooldown--;
		}
		if (this.level() instanceof ServerLevel serverLevel) {
			if (!this.healthInitialized) {
				this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(VANILLA_HEALTH_POINTS);
				this.setHealth(VANILLA_HEALTH_POINTS);
				this.mythicHealth = MAX_HEALTH_POINTS;
				this.nextBombHealthThreshold = MAX_HEALTH_POINTS - BOMB_HEALTH_STEP_POINTS;
				this.nextLightningHealthThreshold = MAX_HEALTH_POINTS - LIGHTNING_HEALTH_STEP_POINTS;
				this.revengeDamageProgress = 0.0F;
				this.revengeStepIndex = 0;
				this.pendingRevengeStage = 0;
				this.revengeComboTicks = 0;
				this.healthInitialized = true;
			}
			this.tickTrackedDeathTarget(serverLevel);
			if (this.deathLootTicks > 0) {
				this.tickDeathLootScene(serverLevel);
				return;
			}
			if (!this.hasSurvivalPlayer(serverLevel)) {
				this.vanish();
				return;
			}
			this.tickNonPlayerAggroCleanup(serverLevel);

			if (this.attackCooldown > 0) {
				this.attackCooldown--;
			}
			if (this.obstacleActionCooldown > 0) {
				this.obstacleActionCooldown--;
			}
			if (this.smartPlanCooldown > 0) {
				this.smartPlanCooldown--;
			}
			if (this.rejectedDoorPlanCooldown > 0) {
				this.rejectedDoorPlanCooldown--;
				if (this.rejectedDoorPlanCooldown == 0) {
					this.rejectedDoorPlanPos = null;
				}
			}
			if (this.directBreachTntCooldown > 0) {
				this.directBreachTntCooldown--;
			}
			if (this.pendingBreachTntPos != null) {
				this.tickPendingBreachTnt(serverLevel);
			}
			if (this.weaponRerollTicks > 0) {
				this.weaponRerollTicks--;
			}
			if (this.tunnelDiggingMode) {
				this.tickTunnelDigging(serverLevel);
				return;
			}
			if (this.blockTaskMode) {
				this.tickBlockTask(serverLevel);
				return;
			}
			if (this.theftTaskMode) {
				this.tickTheftTask(serverLevel);
				return;
			}
			if (this.disguiseAmbushDelayTicks > 0) {
				this.tickDisguiseAmbushDelay(serverLevel);
				return;
			}
			if (this.disguiseCounterTicks > 0) {
				this.tickDisguiseCounterScare(serverLevel);
				return;
			}
			if (this.postKillCooldownTicks > 0) {
				this.postKillCooldownTicks--;
				this.setTarget(null);
				this.getNavigation().stop();
				return;
			}
			if (!this.attackEnabled) {
				this.tickStalking(serverLevel);
				return;
			}

			LivingEntity target = this.getTarget();
			if (target instanceof ServerPlayer player) {
				if (player.isDeadOrDying() || !player.isAlive() || player.getHealth() <= 0.0F) {
					this.handlePlayerDeath(player);
					return;
				}
				if (player.isCreative() || player.isSpectator()) {
					this.setTarget(null);
					this.getNavigation().stop();
					this.resetCombat();
					this.vanish();
					return;
				}
				if (!player.getUUID().equals(this.combatTargetId)) {
					this.startCombatWith(player);
				}
				this.combatTicks++;
				if (this.weaponRerollTicks <= 0 && !this.usingUtilityTool) {
					this.chooseCombatWeapon();
				}
				if (this.tickRevengeCombo(player, serverLevel)) {
					return;
				}
				if (this.tryDefensiveSwordRetreat(player, serverLevel)) {
					return;
				}

				double distance = this.distanceTo(player);
				if (distance >= GIVE_UP_DISTANCE) {
					this.setTarget(null);
					this.getNavigation().stop();
					this.resetCombat();
					return;
				}

				this.getLookControl().setLookAt(player, 30.0F, 30.0F);
				if (distance <= ATTACK_RANGE && this.hasLineOfSight(player)) {
					this.tryFastHandAttack(player, serverLevel);
					this.resetStuckTracker();
				} else {
					this.tickWaterPursuit(player);
					this.tickObstacleFreedom(player, serverLevel);
				}
			} else {
				this.resetCombat();
			}
		}
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
		if (!this.healthInitialized) {
			this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(VANILLA_HEALTH_POINTS);
				this.setHealth(VANILLA_HEALTH_POINTS);
				this.mythicHealth = MAX_HEALTH_POINTS;
				this.nextBombHealthThreshold = MAX_HEALTH_POINTS - BOMB_HEALTH_STEP_POINTS;
				this.nextLightningHealthThreshold = MAX_HEALTH_POINTS - LIGHTNING_HEALTH_STEP_POINTS;
				this.revengeDamageProgress = 0.0F;
				this.revengeStepIndex = 0;
				this.pendingRevengeStage = 0;
				this.revengeComboTicks = 0;
				this.healthInitialized = true;
			}
		ServerPlayer attackingPlayer = damageSource.getEntity() instanceof ServerPlayer player ? player : null;
		if (attackingPlayer != null) {
			this.consecutiveHandHits = 0;
			// نحدد دامج اللاعب حسب نوع السلاح
			ItemStack weapon = attackingPlayer.getMainHandItem();
			String id = weapon.isEmpty() ? "hand" : net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(weapon.getItem()).getPath();
			float cappedDamage;
			if (id.equals("netherite_sword"))     cappedDamage = 4.0F;
			else if (id.equals("diamond_sword"))  cappedDamage = 3.5F;
			else if (id.equals("iron_sword") || id.equals("golden_sword")) cappedDamage = 3.0F;
			else if (id.equals("stone_sword"))    cappedDamage = 2.5F;
			else if (id.equals("wooden_sword") || id.equals("copper_sword")) cappedDamage = 2.0F;
			else if (id.endsWith("_axe"))         cappedDamage = id.startsWith("netherite") ? 4.0F : id.startsWith("diamond") ? 3.5F : id.startsWith("iron") ? 3.0F : id.startsWith("stone") ? 2.5F : 2.0F;
			else cappedDamage = 1.0F;
			amount = cappedDamage;
		}

		this.mythicHealth -= amount;
		if (attackingPlayer != null) {
			this.trackRevengeDamage(amount, attackingPlayer, serverLevel);
		}
		this.triggerBombVolleyIfNeeded(serverLevel);
		boolean hurt = super.hurtServer(serverLevel, damageSource, Math.min(amount, 2.0F));
		this.setHealth(VANILLA_HEALTH_POINTS);
		this.applyReducedNaturalKnockback(damageSource);

		if (this.mythicHealth <= VANISH_HEALTH_POINTS) {
			this.vanishWithEndermanSound();
		}

		return hurt;
	}

	@Override
	public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
		return this.performHandHit(serverLevel, target);
	}

	private void tryFastHandAttack(ServerPlayer player, ServerLevel serverLevel) {
		if (this.attackCooldown > 0 || !this.hasLineOfSight(player)) {
			return;
		}
		this.clearBreakingState(serverLevel);
		if (this.usingUtilityTool) {
			this.chooseCombatWeapon();
		}
		if (this.performHandHit(serverLevel, player)) {
			this.attackCooldown = ATTACK_COOLDOWN_TICKS;
		}
	}

	private boolean performHandHit(ServerLevel serverLevel, Entity target) {
		this.swing(InteractionHand.MAIN_HAND, true);
		float damage = (target instanceof ServerPlayer player) ? this.calcWeaponDamage(player) : (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
		boolean hit = target.hurtServer(serverLevel, serverLevel.damageSources().mobAttack(this), damage);
		if (hit && target instanceof ServerPlayer player) {
			// نلغي الـ vanilla knockback ونحط قيمة ثابتة صغيرة = نص بلوكة
			Vec3 away = player.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
			if (away.lengthSqr() > 0.0001D) {
				Vec3 push = away.normalize().scale(0.35D);
				player.setDeltaMovement(push.x, 0.1D, push.z);
				player.hurtMarked = true;
			}
			if (this.random.nextFloat() < 0.12F) {
				this.triggerPortalDistortion(serverLevel, player);
			}

			this.consecutiveHandHits++;
			if (this.consecutiveHandHits >= ARMOR_DROP_HIT_COUNT) {
				this.dropPlayerArmor(player, serverLevel);
				this.consecutiveHandHits = 0;
			}
			if (player.isDeadOrDying() || !player.isAlive() || player.getHealth() <= 0.0F) {
				this.handlePlayerDeath(player);
			}
		}
		return hit;
	}

	@Override
	public void die(DamageSource damageSource) {
		this.vanishWithEndermanSound();
	}

	private int knockbackCooldown = 0;

	@Override
	public void knockback(double strength, double x, double z) {
		if (this.knockbackCooldown > 0) {
			return;
		}
		double length = Math.sqrt(x * x + z * z);
		if (length > 0.0001D) {
			Vec3 current = this.getDeltaMovement();
			this.setDeltaMovement(
				-(x / length) * 0.12D,
				current.y + 0.08D,
				-(z / length) * 0.12D
			);
			this.hurtMarked = true;
		}
		this.knockbackCooldown = 10;
	}

	@Override
	public void setDeltaMovement(Vec3 movement) {
		// نمنع أي horizontal velocity أكبر من 0.15 عشان الـ knockback ميتراكمش
		double maxH = 0.15D;
		double clampedX = Math.max(-maxH, Math.min(maxH, movement.x));
		double clampedZ = Math.max(-maxH, Math.min(maxH, movement.z));
		super.setDeltaMovement(new Vec3(clampedX, movement.y, clampedZ));
	}

	private void vanish() {
		if (this.level() instanceof ServerLevel serverLevel) {
			this.clearTunnelDiggingState(serverLevel);
			this.clearBlockTaskState(serverLevel);
			serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 60, 0.4D, 0.8D, 0.4D, 0.04D);
			this.playFullVanishSound(serverLevel);
		}
		this.discard();
	}

	private void vanishWithCave2() {
		if (this.level() instanceof ServerLevel serverLevel) {
			this.clearTunnelDiggingState(serverLevel);
			this.clearBlockTaskState(serverLevel);
			serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 60, 0.4D, 0.8D, 0.4D, 0.04D);
			this.playFullVanishSound(serverLevel);
		}
		this.discard();
	}

	private void vanishWithEndermanSound() {
		if (this.level() instanceof ServerLevel serverLevel) {
			this.clearTunnelDiggingState(serverLevel);
			this.clearBlockTaskState(serverLevel);
			serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 60, 0.4D, 0.8D, 0.4D, 0.04D);
			this.playFullVanishSound(serverLevel);
		}
		this.discard();
	}

	private void playFullVanishSound(ServerLevel serverLevel) {
		serverLevel.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.75F);
		this.playVanishSound(serverLevel);
	}

	private void playVanishSound(ServerLevel serverLevel) {
		Player nearestPlayer = this.level().getNearestPlayer(this, 160.0D);
		if (nearestPlayer != null) {
			serverLevel.playSound(null, nearestPlayer.blockPosition(), ModSounds.CAVE_2, SoundSource.MASTER, 2.25F, 1.0F);
			return;
		}
		serverLevel.playSound(null, this.blockPosition(), ModSounds.CAVE_2, SoundSource.MASTER, 2.0F, 1.0F);
	}

	public void vanishFromWorld() {
		this.vanish();
	}

	private boolean hasSurvivalPlayer(ServerLevel serverLevel) {
		for (ServerPlayer player : serverLevel.players()) {
			if (this.isSurvivalPlayer(player)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSurvivalPlayer(ServerPlayer player) {
		return player.isAlive() && !player.isDeadOrDying() && !player.isCreative() && !player.isSpectator();
	}

	public void setStalkingMode(int ticks) {
		this.setStalkingMode(ticks, false);
	}

	public void setStalkingMode(int ticks, boolean aggressive) {
		this.setStalkingMode(ticks, aggressive, aggressive ? 0.25F : 0.0F, null);
	}

	public void setStalkingMode(int ticks, boolean aggressive, float attackChance, BlockPos watchPos) {
		this.attackEnabled = false;
		this.tunnelDiggingMode = false;
		this.blockTaskMode = false;
		this.stalkingTicks = ticks;
		this.aggressiveStalking = aggressive;
		this.aggressiveStalkingAttackChance = attackChance;
		this.stalkingReactionUsed = false;
		this.stalkingWatchPos = watchPos == null ? null : watchPos.immutable();
		this.stalkingRoamTarget = null;
		this.stalkingSettled = watchPos == null;
		this.stalkingTravelTicks = 0;
		this.stalkingRoamCooldown = 0;
		this.stalkingCoverStep = 0;
		this.stalkingCoverCooldown = 0;
		this.setNoAi(false);
		this.setTarget(null);
		this.getNavigation().stop();
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	public void setTunnelDiggingMode(BlockPos startPos, Direction direction, int length) {
		this.tunnelDiggingMode = true;
		this.theftTaskMode = false;
		this.attackEnabled = true;
		this.tunnelStartPos = startPos.immutable();
		this.tunnelDirection = direction;
		this.tunnelLength = Math.max(4, length);
		this.tunnelBlockIndex = 0;
		this.tunnelCurrentBlockPos = null;
		this.tunnelBreakingTicks = 0;
		this.tunnelBreakingTotalTicks = 0;
		this.setNoAi(true);
		this.setTarget(null);
		this.getNavigation().stop();
		this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
	}

	public void setBlockTaskMode(List<BlockPos> breakPositions, List<BlockPlacement> placements) {
		this.blockTaskMode = true;
		this.tunnelDiggingMode = false;
		this.theftTaskMode = false;
		this.attackEnabled = true;
		this.taskBreakPositions = List.copyOf(breakPositions);
		this.taskPlacements = List.copyOf(placements);
		this.taskBreakIndex = 0;
		this.taskPlaceIndex = 0;
		this.taskBreakingBlockPos = null;
		this.taskBreakingTicks = 0;
		this.taskBreakingTotalTicks = 0;
		this.taskPlaceCooldown = 0;
		this.setNoAi(true);
		this.setTarget(null);
		this.getNavigation().stop();
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	public void setTheftTaskMode(BlockPos chestPos, int ticks) {
		this.theftTaskMode = true;
		this.blockTaskMode = false;
		this.tunnelDiggingMode = false;
		this.attackEnabled = true;
		this.theftTargetPos = chestPos.immutable();
		this.theftDoorPos = null;
		this.theftWanderPos = null;
		this.theftTaskTicks = Math.max(20 * 20, ticks);
		this.theftTaskPhase = 0;
		this.setNoAi(false);
		this.setTarget(null);
		this.getNavigation().stop();
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	public void setDisguiseAmbushAttack(ServerPlayer player, int delayTicks) {
		this.disguiseAmbushTargetId = player.getUUID();
		this.disguiseAmbushDelayTicks = Math.max(1, delayTicks);
		this.disguiseCounterTargetId = null;
		this.disguiseCounterTicks = 0;
		this.disguiseCounterHitDone = false;
		this.attackEnabled = false;
		this.setNoAi(true);
		this.setTarget(null);
		this.getNavigation().stop();
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	public void setDisguiseCounterScare(ServerPlayer player) {
		this.disguiseCounterTargetId = player.getUUID();
		this.disguiseCounterTicks = 20 * 3;
		this.disguiseCounterHitDone = false;
		this.disguiseAmbushTargetId = null;
		this.disguiseAmbushDelayTicks = 0;
		this.attackEnabled = false;
		this.setNoAi(true);
		this.setTarget(null);
		this.getNavigation().stop();
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	private void tickStalking(ServerLevel serverLevel) {
		this.setTarget(null);
		Player player = this.level().getNearestPlayer(this, STALKING_MAX_DISTANCE);
		if (player != null) {
			if (this.distanceTo(player) > STALKING_MAX_DISTANCE) {
				this.stalkingVanishCue = true;
				this.vanishWithCave2();
				return;
			}
			if (!this.stalkingSettled) {
				this.tickMovingToStalkingSpot(serverLevel, player);
				return;
			}

			this.tickLivingStalkingRoam(serverLevel, player);
			if (this.getNavigation().isDone() || this.distanceTo(player) < 35.0F) {
				this.faceStalkingPlayer(player);
			}
			if (this.playerCaughtStalking(player)) {
				if (this.tryAggressiveStalkingAttack(player)) {
					return;
				}
				this.stalkingVanishCue = true;
				this.vanishWithCave2();
				return;
			}
		}

		this.stalkingTicks--;
		if (this.stalkingTicks <= 0) {
			this.stalkingVanishCue = true;
			this.vanishWithCave2();
		}
	}

	private void tickDisguiseAmbushDelay(ServerLevel serverLevel) {
		ServerPlayer player = this.disguiseAmbushTargetId == null ? null : serverLevel.getServer().getPlayerList().getPlayer(this.disguiseAmbushTargetId);
		if (player == null || !this.isSurvivalPlayer(player)) {
			this.disguiseAmbushTargetId = null;
			this.disguiseAmbushDelayTicks = 0;
			this.vanishWithEndermanSound();
			return;
		}

		this.setTarget(null);
		this.getNavigation().stop();
		this.setNoAi(true);
		this.getLookControl().setLookAt(player, 30.0F, 30.0F);
		this.disguiseAmbushDelayTicks--;
		if (this.disguiseAmbushDelayTicks <= 0) {
			this.attackEnabled = true;
			this.setNoAi(false);
			this.setTarget(player);
			this.startCombatWith(player);
			this.disguiseAmbushTargetId = null;
		}
	}

	private void tickDisguiseCounterScare(ServerLevel serverLevel) {
		ServerPlayer player = this.disguiseCounterTargetId == null ? null : serverLevel.getServer().getPlayerList().getPlayer(this.disguiseCounterTargetId);
		if (player == null || !this.isSurvivalPlayer(player)) {
			this.disguiseCounterTargetId = null;
			this.disguiseCounterTicks = 0;
			this.disguiseCounterHitDone = false;
			this.vanishWithEndermanSound();
			return;
		}

		this.setTarget(null);
		this.getNavigation().stop();
		this.setNoAi(true);
		this.getLookControl().setLookAt(player, 30.0F, 30.0F);
		if (!this.disguiseCounterHitDone) {
			this.disguiseCounterHitDone = true;
			this.swing(InteractionHand.MAIN_HAND, true);
			player.hurtServer(serverLevel, serverLevel.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
			Vec3 away = player.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
			if (away.lengthSqr() < 0.0001D) {
				away = player.getLookAngle().multiply(-1.0D, 0.0D, -1.0D);
			}
			if (away.lengthSqr() < 0.0001D) {
				away = new Vec3(1.0D, 0.0D, 0.0D);
			}
			away = away.normalize();
			player.push(away.x * 1.55D, 0.35D, away.z * 1.55D);
			serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.8D, player.getZ(), 18, 0.25D, 0.35D, 0.25D, 0.025D);
			if (player.isDeadOrDying() || !player.isAlive() || player.getHealth() <= 0.0F) {
				this.handlePlayerDeath(player);
			}
		}

		this.disguiseCounterTicks--;
		if (this.disguiseCounterTicks <= 0) {
			this.disguiseCounterTargetId = null;
			this.disguiseCounterHitDone = false;
			this.vanishWithEndermanSound();
		}
	}

	private void tickMovingToStalkingSpot(ServerLevel serverLevel, Player player) {
		this.setNoAi(false);
		if (this.stalkingWatchPos == null) {
			this.stalkingSettled = true;
			this.stalkingRoamCooldown = STALKING_ROAM_MIN_COOLDOWN_TICKS;
			return;
		}

		double dx = this.getX() - (this.stalkingWatchPos.getX() + 0.5D);
		double dy = this.getY() - this.stalkingWatchPos.getY();
		double dz = this.getZ() - (this.stalkingWatchPos.getZ() + 0.5D);
		double distanceSqr = dx * dx + dy * dy + dz * dz;
		if (distanceSqr > 2.25D) {
			this.getNavigation().moveTo(this.stalkingWatchPos.getX() + 0.5D, this.stalkingWatchPos.getY(), this.stalkingWatchPos.getZ() + 0.5D, 0.95D);
			this.getLookControl().setLookAt(this.stalkingWatchPos.getX() + 0.5D, this.stalkingWatchPos.getY() + 1.0D, this.stalkingWatchPos.getZ() + 0.5D, 20.0F, 20.0F);
			this.stalkingTravelTicks++;
			if (this.stalkingTravelTicks > 20 * 90) {
				this.vanish();
			}
			return;
		}

		this.getNavigation().stop();
		this.stalkingSettled = true;
		this.setNoAi(false);
		this.stalkingRoamCooldown = STALKING_ROAM_MIN_COOLDOWN_TICKS;
	}

	private void tickLivingStalkingRoam(ServerLevel serverLevel, Player player) {
		this.setNoAi(false);
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		if (this.stalkingRoamCooldown > 0) {
			this.stalkingRoamCooldown--;
		}

		boolean needsNewTarget = this.stalkingRoamTarget == null
				|| this.getNavigation().isDone()
				|| this.distanceToSqr(Vec3.atCenterOf(this.stalkingRoamTarget)) < 3.0D
				|| this.stalkingRoamCooldown <= 0;
		if (needsNewTarget) {
			this.stalkingRoamTarget = this.findStalkingRoamTarget(serverLevel, player);
			this.stalkingRoamCooldown = STALKING_ROAM_MIN_COOLDOWN_TICKS + this.random.nextInt(STALKING_ROAM_RANDOM_COOLDOWN_TICKS + 1);
		}

		if (this.stalkingRoamTarget != null) {
			this.getNavigation().moveTo(this.stalkingRoamTarget.getX() + 0.5D, this.stalkingRoamTarget.getY(), this.stalkingRoamTarget.getZ() + 0.5D, 0.72D);
		}
	}

	private BlockPos findStalkingRoamTarget(ServerLevel serverLevel, Player player) {
		BlockPos anchor = this.stalkingWatchPos == null ? this.blockPosition() : this.stalkingWatchPos;
		BlockPos playerPos = player.blockPosition();
		for (int attempt = 0; attempt < 36; attempt++) {
			double angle = this.random.nextDouble() * Math.PI * 2.0D;
			double radius = 6.0D + this.random.nextDouble() * 18.0D;
			BlockPos origin = BlockPos.containing(anchor.getX() + Math.cos(angle) * radius, anchor.getY(), anchor.getZ() + Math.sin(angle) * radius);
			for (int yOffset = 6; yOffset >= -6; yOffset--) {
				BlockPos candidate = origin.offset(0, yOffset, 0);
				double playerDistanceSqr = candidate.distSqr(playerPos);
				if (playerDistanceSqr < 12.0D * 12.0D || playerDistanceSqr > STALKING_MAX_DISTANCE * STALKING_MAX_DISTANCE) {
					continue;
				}
				if (this.canSafeStalkingRoamAt(serverLevel, candidate)) {
					return candidate.immutable();
				}
			}
		}
		return anchor;
	}

	private boolean canSafeStalkingRoamAt(ServerLevel serverLevel, BlockPos pos) {
		if (!this.canStandAt(serverLevel, pos)) {
			return false;
		}
		BlockState feet = serverLevel.getBlockState(pos);
		BlockState head = serverLevel.getBlockState(pos.above());
		BlockState floor = serverLevel.getBlockState(pos.below());
		return !feet.liquid()
				&& !head.liquid()
				&& !floor.liquid()
				&& !floor.typeHolder().is(BlockTags.LEAVES)
				&& !floor.typeHolder().is(BlockTags.LOGS);
	}

	private boolean tickBuildStalkingCover(ServerLevel serverLevel, Player player) {
		if (this.stalkingCoverStep >= 2 || this.hasStalkingCoverTowardPlayer(serverLevel, player)) {
			this.stalkingCoverStep = 2;
			this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			return false;
		}
		if (this.stalkingCoverCooldown > 0) {
			this.stalkingCoverCooldown--;
			return true;
		}

		BlockPos coverPos = this.getStalkingCoverPos(player, this.stalkingCoverStep == 0 ? 0 : 1);
		if (coverPos == null || !this.canPlaceStalkingCover(serverLevel, coverPos)) {
			this.stalkingCoverStep = 2;
			this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			return false;
		}

		this.faceBlock(coverPos);
		this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Blocks.DIRT));
		this.swing(InteractionHand.MAIN_HAND, true);
		serverLevel.setBlockAndUpdate(coverPos, Blocks.DIRT.defaultBlockState());
		serverLevel.playSound(null, coverPos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.7F, 0.75F);
		this.stalkingCoverStep++;
		this.stalkingCoverCooldown = 10;
		return true;
	}

	private boolean hasStalkingCoverTowardPlayer(ServerLevel serverLevel, Player player) {
		BlockPos base = this.blockPosition();
		int xStep = Integer.compare(player.blockPosition().getX(), base.getX());
		int zStep = Integer.compare(player.blockPosition().getZ(), base.getZ());
		if (xStep == 0 && zStep == 0) {
			return true;
		}
		for (int step = 1; step <= 3; step++) {
			BlockPos pos = base.offset(xStep * step, 0, zStep * step);
			BlockState state = serverLevel.getBlockState(pos);
			BlockState above = serverLevel.getBlockState(pos.above());
			if ((!state.isAir() && !state.getCollisionShape(serverLevel, pos).isEmpty())
					|| (!above.isAir() && !above.getCollisionShape(serverLevel, pos.above()).isEmpty())) {
				return true;
			}
		}
		return false;
	}

	private BlockPos getStalkingCoverPos(Player player, int height) {
		BlockPos base = this.blockPosition();
		int xStep = Integer.compare(player.blockPosition().getX(), base.getX());
		int zStep = Integer.compare(player.blockPosition().getZ(), base.getZ());
		if (xStep == 0 && zStep == 0) {
			return null;
		}
		return base.offset(xStep, height, zStep);
	}

	private boolean canPlaceStalkingCover(ServerLevel serverLevel, BlockPos pos) {
		return serverLevel.getBlockState(pos).isAir()
				&& (pos.getY() == this.blockPosition().getY() || !serverLevel.getBlockState(pos.below()).getCollisionShape(serverLevel, pos.below()).isEmpty());
	}

	private void tickTunnelDigging(ServerLevel serverLevel) {
		this.setTarget(null);
		this.getNavigation().stop();
		this.setNoAi(true);

		if (this.tunnelStartPos == null || this.tunnelDirection == null || this.tunnelBlockIndex >= this.tunnelLength * 4) {
			this.finishTunnelDigging(serverLevel);
			return;
		}

		if (this.tunnelCurrentBlockPos == null) {
			this.tunnelCurrentBlockPos = this.getTunnelBlockAtIndex(this.tunnelBlockIndex);
			this.tunnelBreakingTicks = 0;
			BlockState state = serverLevel.getBlockState(this.tunnelCurrentBlockPos);
			if (state.isAir()) {
				this.advanceTunnelBlock(serverLevel);
				return;
			}
			if (!this.canTunnelDigThrough(serverLevel, this.tunnelCurrentBlockPos, state)) {
				this.finishTunnelDigging(serverLevel);
				return;
			}
			this.tunnelBreakingTotalTicks = this.getSurvivalBreakTicks(state, serverLevel, this.tunnelCurrentBlockPos);
			this.setItemInHand(InteractionHand.MAIN_HAND, this.getToolForBlock(state));
			this.faceBlock(this.tunnelCurrentBlockPos);
			this.swing(InteractionHand.MAIN_HAND, true);
			serverLevel.destroyBlockProgress(this.getId(), this.tunnelCurrentBlockPos, 0);
		}

		BlockState state = serverLevel.getBlockState(this.tunnelCurrentBlockPos);
		if (state.isAir()) {
			this.advanceTunnelBlock(serverLevel);
			return;
		}
		if (!this.canTunnelDigThrough(serverLevel, this.tunnelCurrentBlockPos, state)) {
			this.finishTunnelDigging(serverLevel);
			return;
		}

		this.faceBlock(this.tunnelCurrentBlockPos);
		if (this.tunnelBreakingTicks % 8 == 0) {
			this.swing(InteractionHand.MAIN_HAND, true);
		}

		this.tunnelBreakingTicks++;
		int progress = Math.min(9, (int) ((this.tunnelBreakingTicks * 10.0F) / Math.max(1, this.tunnelBreakingTotalTicks)));
		serverLevel.destroyBlockProgress(this.getId(), this.tunnelCurrentBlockPos, progress);

		if (this.tunnelBreakingTicks >= this.tunnelBreakingTotalTicks) {
			BlockPos brokenPos = this.tunnelCurrentBlockPos;
			serverLevel.levelEvent(null, 2001, brokenPos, Block.getId(state));
			serverLevel.destroyBlock(brokenPos, false, this, 512);
			this.advanceTunnelBlock(serverLevel);
		}
	}

	private BlockPos getTunnelBlockAtIndex(int index) {
		Direction right = this.rightOf(this.tunnelDirection);
		int step = index / 4;
		int local = index % 4;
		int width = local / 2;
		int height = local % 2;
		return this.tunnelStartPos.relative(this.tunnelDirection, step).relative(right, width).above(height);
	}

	private void advanceTunnelBlock(ServerLevel serverLevel) {
		if (this.tunnelCurrentBlockPos != null) {
			serverLevel.destroyBlockProgress(this.getId(), this.tunnelCurrentBlockPos, -1);
		}
		this.tunnelBlockIndex++;
		this.tunnelCurrentBlockPos = null;
		this.tunnelBreakingTicks = 0;
		this.tunnelBreakingTotalTicks = 0;
		this.moveAlongTunnel(serverLevel);
	}

	private void moveAlongTunnel(ServerLevel serverLevel) {
		if (this.tunnelStartPos == null || this.tunnelDirection == null || this.tunnelBlockIndex < 4) {
			return;
		}
		int completedStep = Math.max(0, Math.min(this.tunnelLength - 1, this.tunnelBlockIndex / 4 - 1));
		BlockPos standPos = this.tunnelStartPos.relative(this.tunnelDirection, completedStep);
		if (this.canStandAt(serverLevel, standPos)) {
			this.teleportTo(standPos.getX() + 0.5D, standPos.getY(), standPos.getZ() + 0.5D);
		}
	}

	private void finishTunnelDigging(ServerLevel serverLevel) {
		this.clearTunnelDiggingState(serverLevel);
		this.vanish();
	}

	private void tickBlockTask(ServerLevel serverLevel) {
		this.setTarget(null);
		this.getNavigation().stop();
		this.setNoAi(true);

		if (this.taskBreakIndex < this.taskBreakPositions.size()) {
			this.tickTaskBreaking(serverLevel);
			return;
		}

		if (this.taskPlaceIndex < this.taskPlacements.size()) {
			this.tickTaskPlacement(serverLevel);
			return;
		}

		this.clearBlockTaskState(serverLevel);
		this.vanish();
	}

	private void tickTaskBreaking(ServerLevel serverLevel) {
		if (this.taskBreakingBlockPos == null) {
			this.taskBreakingBlockPos = this.taskBreakPositions.get(this.taskBreakIndex);
			this.taskBreakingTicks = 0;
			BlockState state = serverLevel.getBlockState(this.taskBreakingBlockPos);
			if (state.isAir()) {
				this.advanceTaskBreak(serverLevel);
				return;
			}
			if (!this.isBreakableObstacle(state, serverLevel, this.taskBreakingBlockPos)) {
				this.advanceTaskBreak(serverLevel);
				return;
			}
			this.taskBreakingTotalTicks = this.getSurvivalBreakTicks(state, serverLevel, this.taskBreakingBlockPos);
			this.setItemInHand(InteractionHand.MAIN_HAND, this.getToolForBlock(state));
			this.faceBlock(this.taskBreakingBlockPos);
			this.swing(InteractionHand.MAIN_HAND, true);
			serverLevel.destroyBlockProgress(this.getId(), this.taskBreakingBlockPos, 0);
		}

		BlockState state = serverLevel.getBlockState(this.taskBreakingBlockPos);
		if (state.isAir() || !this.isBreakableObstacle(state, serverLevel, this.taskBreakingBlockPos)) {
			this.advanceTaskBreak(serverLevel);
			return;
		}

		this.faceBlock(this.taskBreakingBlockPos);
		if (this.taskBreakingTicks % 8 == 0) {
			this.swing(InteractionHand.MAIN_HAND, true);
		}

		this.taskBreakingTicks++;
		int progress = Math.min(9, (int) ((this.taskBreakingTicks * 10.0F) / Math.max(1, this.taskBreakingTotalTicks)));
		serverLevel.destroyBlockProgress(this.getId(), this.taskBreakingBlockPos, progress);

		if (this.taskBreakingTicks >= this.taskBreakingTotalTicks) {
			BlockPos brokenPos = this.taskBreakingBlockPos;
			serverLevel.levelEvent(null, 2001, brokenPos, Block.getId(state));
			serverLevel.destroyBlock(brokenPos, false, this, 512);
			this.advanceTaskBreak(serverLevel);
		}
	}

	private void advanceTaskBreak(ServerLevel serverLevel) {
		if (this.taskBreakingBlockPos != null) {
			serverLevel.destroyBlockProgress(this.getId(), this.taskBreakingBlockPos, -1);
		}
		this.taskBreakIndex++;
		this.taskBreakingBlockPos = null;
		this.taskBreakingTicks = 0;
		this.taskBreakingTotalTicks = 0;
	}

	private void tickTaskPlacement(ServerLevel serverLevel) {
		if (this.taskPlaceCooldown > 0) {
			this.taskPlaceCooldown--;
			return;
		}

		BlockPlacement placement = this.taskPlacements.get(this.taskPlaceIndex);
		this.faceBlock(placement.pos());
		this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(placement.state().getBlock()));
		this.swing(InteractionHand.MAIN_HAND, true);
		if (this.canReplacePlacedTaskBlock(serverLevel, placement.pos())) {
			serverLevel.setBlockAndUpdate(placement.pos(), placement.state());
			serverLevel.playSound(null, placement.pos(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
		}
		this.taskPlaceIndex++;
		this.taskPlaceCooldown = 8;
	}

	private boolean canReplacePlacedTaskBlock(ServerLevel serverLevel, BlockPos pos) {
		BlockState state = serverLevel.getBlockState(pos);
		return state.isAir() || state.getCollisionShape(serverLevel, pos).isEmpty();
	}

	private void clearBlockTaskState(ServerLevel serverLevel) {
		if (serverLevel != null && this.taskBreakingBlockPos != null) {
			serverLevel.destroyBlockProgress(this.getId(), this.taskBreakingBlockPos, -1);
		}
		this.blockTaskMode = false;
		this.taskBreakPositions = List.of();
		this.taskPlacements = List.of();
		this.taskBreakingBlockPos = null;
		this.taskBreakIndex = 0;
		this.taskPlaceIndex = 0;
		this.taskBreakingTicks = 0;
		this.taskBreakingTotalTicks = 0;
		this.taskPlaceCooldown = 0;
		this.setNoAi(false);
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	private void tickTheftTask(ServerLevel serverLevel) {
		this.setTarget(null);
		ServerPlayer nearestPlayer = this.level().getNearestPlayer(this, STALKING_MAX_DISTANCE) instanceof ServerPlayer player ? player : null;
		if (nearestPlayer != null) {
			this.tryOpenNearbyDoor(nearestPlayer, serverLevel);
		}
		if (this.theftTargetPos == null) {
			this.clearTheftTaskState();
			this.vanishWithEndermanSound();
			return;
		}

		if (this.theftTaskPhase == 0) {
			this.setNoAi(false);
			if (this.theftDoorPos == null) {
				this.theftDoorPos = this.findTheftDoorNearTarget(serverLevel);
			}

			if (this.theftDoorPos != null) {
				this.faceBlock(this.theftDoorPos);
				double doorDistanceSqr = this.distanceToSqr(this.theftDoorPos.getX() + 0.5D, this.theftDoorPos.getY(), this.theftDoorPos.getZ() + 0.5D);
				if (doorDistanceSqr > THEFT_DOOR_REACH_DISTANCE_SQR) {
					this.getNavigation().moveTo(this.theftDoorPos.getX() + 0.5D, this.theftDoorPos.getY(), this.theftDoorPos.getZ() + 0.5D, 0.86D);
					this.theftTaskTicks--;
					if (this.theftTaskTicks <= 0) {
						this.clearTheftTaskState();
						this.vanishWithEndermanSound();
					}
					return;
				}

				if (!this.teleportToTheftContainerStand(serverLevel)) {
					this.theftDoorPos = null;
					return;
				}
			} else {
				this.faceBlock(this.theftTargetPos);
				double distanceSqr = this.distanceToSqr(this.theftTargetPos.getX() + 0.5D, this.theftTargetPos.getY(), this.theftTargetPos.getZ() + 0.5D);
				if (distanceSqr > 8.0D) {
					BlockPos standPos = this.findTheftContainerStandPos(serverLevel);
					BlockPos moveTarget = standPos != null ? standPos : this.theftTargetPos;
					this.getNavigation().moveTo(moveTarget.getX() + 0.5D, moveTarget.getY(), moveTarget.getZ() + 0.5D, 0.86D);
					this.theftTaskTicks--;
					if (this.theftTaskTicks <= 0) {
						this.clearTheftTaskState();
						this.vanishWithEndermanSound();
					}
					return;
				}
				this.teleportToTheftContainerStand(serverLevel);
			}

			this.getNavigation().stop();
			this.setNoAi(true);
			this.performTheftAtTarget(serverLevel);
			this.theftTaskPhase = 1;
			this.theftTaskTicks = 20 * 4;
			return;
		}

		if (this.theftTaskPhase == 1) {
			this.getNavigation().stop();
			this.setNoAi(true);
			this.faceBlock(this.theftTargetPos);
			if (this.theftTaskTicks % 16 == 0) {
				this.swing(InteractionHand.MAIN_HAND, true);
			}
			this.theftTaskTicks--;
			if (this.theftTaskTicks <= 0) {
				this.closeTheftContainer(serverLevel);
				this.theftWanderPos = this.findTheftWanderPos(serverLevel);
				this.theftTaskPhase = 2;
				this.theftTaskTicks = 20 * 6;
				this.setNoAi(false);
			}
			return;
		}

		if (this.theftTaskPhase == 2) {
			this.setNoAi(false);
			if (this.theftWanderPos != null && this.distanceToSqr(this.theftWanderPos.getX() + 0.5D, this.theftWanderPos.getY(), this.theftWanderPos.getZ() + 0.5D) > 2.25D) {
				this.getNavigation().moveTo(this.theftWanderPos.getX() + 0.5D, this.theftWanderPos.getY(), this.theftWanderPos.getZ() + 0.5D, 0.85D);
				this.getLookControl().setLookAt(this.theftWanderPos.getX() + 0.5D, this.theftWanderPos.getY() + 1.0D, this.theftWanderPos.getZ() + 0.5D);
			} else {
				this.faceBlock(this.theftTargetPos);
			}
			this.theftTaskTicks--;
			if (this.theftTaskTicks <= 0) {
				this.clearTheftTaskState();
				this.vanishWithEndermanSound();
			}
		}
	}

	private BlockPos findTheftDoorNearTarget(ServerLevel serverLevel) {
		if (this.theftTargetPos == null) {
			return null;
		}
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (int y = -3; y <= 3; y++) {
			for (int x = -THEFT_DOOR_SCAN_RADIUS; x <= THEFT_DOOR_SCAN_RADIUS; x++) {
				for (int z = -THEFT_DOOR_SCAN_RADIUS; z <= THEFT_DOOR_SCAN_RADIUS; z++) {
					BlockPos pos = this.theftTargetPos.offset(x, y, z);
					BlockState state = serverLevel.getBlockState(pos);
					if (!(state.getBlock() instanceof DoorBlock)) {
						continue;
					}
					double score = pos.distSqr(this.blockPosition()) + pos.distSqr(this.theftTargetPos) * 0.35D;
					if (score < bestScore) {
						bestScore = score;
						best = pos.immutable();
					}
				}
			}
		}
		return best;
	}

	private boolean teleportToTheftContainerStand(ServerLevel serverLevel) {
		BlockPos standPos = this.findTheftContainerStandPos(serverLevel);
		if (standPos == null) {
			return false;
		}
		serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 14, 0.25D, 0.45D, 0.25D, 0.02D);
		this.teleportTo(standPos.getX() + 0.5D, standPos.getY(), standPos.getZ() + 0.5D);
		this.faceBlock(this.theftTargetPos);
		serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 18, 0.25D, 0.45D, 0.25D, 0.02D);
		serverLevel.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.45F, 0.8F);
		return true;
	}

	private BlockPos findTheftContainerStandPos(ServerLevel serverLevel) {
		if (this.theftTargetPos == null) {
			return null;
		}
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos origin = this.theftTargetPos.relative(direction);
			for (int yOffset = 2; yOffset >= -2; yOffset--) {
				BlockPos candidate = origin.offset(0, yOffset, 0);
				if (!this.canStandAt(serverLevel, candidate)) {
					continue;
				}
				double score = candidate.distSqr(this.blockPosition());
				if (this.theftDoorPos != null) {
					score += candidate.distSqr(this.theftDoorPos) * 0.5D;
				}
				if (score < bestScore) {
					bestScore = score;
					best = candidate.immutable();
				}
			}
		}
		if (best != null) {
			return best;
		}
		for (int radius = 2; radius <= 4; radius++) {
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					if (Math.abs(x) != radius && Math.abs(z) != radius) {
						continue;
					}
					BlockPos origin = this.theftTargetPos.offset(x, 0, z);
					for (int yOffset = 2; yOffset >= -2; yOffset--) {
						BlockPos candidate = origin.offset(0, yOffset, 0);
						if (this.canStandAt(serverLevel, candidate)) {
							return candidate.immutable();
						}
					}
				}
			}
		}
		return null;
	}

	private void performTheftAtTarget(ServerLevel serverLevel) {
		BlockEntity blockEntity = serverLevel.getBlockEntity(this.theftTargetPos);
		if (!(blockEntity instanceof Container container)) {
			return;
		}
		BlockState state = serverLevel.getBlockState(this.theftTargetPos);
		serverLevel.blockEvent(this.theftTargetPos, state.getBlock(), 1, 1);
		serverLevel.playSound(null, this.theftTargetPos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.9F, 0.75F);
		serverLevel.playSound(null, this.theftTargetPos, ModSounds.CAVE_5, SoundSource.HOSTILE, 1.25F, 1.0F);
		serverLevel.sendParticles(ParticleTypes.SMOKE, this.theftTargetPos.getX() + 0.5D, this.theftTargetPos.getY() + 0.9D, this.theftTargetPos.getZ() + 0.5D, 18, 0.25D, 0.2D, 0.25D, 0.02D);
		if (this.stealFromTheftContainer(container, THEFT_TASK_MAX_STOLEN_SLOTS) > 0) {
			container.setChanged();
		}
	}

	private void closeTheftContainer(ServerLevel serverLevel) {
		if (this.theftTargetPos == null) {
			return;
		}
		BlockState state = serverLevel.getBlockState(this.theftTargetPos);
		serverLevel.blockEvent(this.theftTargetPos, state.getBlock(), 1, 0);
		serverLevel.playSound(null, this.theftTargetPos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.7F, 0.8F);
	}

	private int stealFromTheftContainer(Container container, int maxSlots) {
		List<Integer> valuableSlots = new ArrayList<>();
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			if (this.isValuableForTheft(container.getItem(slot))) {
				valuableSlots.add(slot);
			}
		}

		int stolenSlots = 0;
		while (!valuableSlots.isEmpty() && stolenSlots < maxSlots) {
			int slotIndex = this.random.nextInt(valuableSlots.size());
			int slot = valuableSlots.remove(slotIndex);
			ItemStack stack = container.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			int takeCount = this.getTheftTakeCount(stack);
			if (takeCount <= 0) {
				continue;
			}
			stack.shrink(takeCount);
			if (stack.isEmpty()) {
				container.setItem(slot, ItemStack.EMPTY);
			}
			stolenSlots++;
		}
		return stolenSlots;
	}

	private int getTheftTakeCount(ItemStack stack) {
		String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
		if (this.isSingleValuableItem(path) || stack.getCount() == 1) {
			return 1;
		}
		return Math.max(1, Math.min(8, stack.getCount() / 4));
	}

	private boolean isValuableForTheft(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
		return path.contains("diamond")
				|| path.contains("emerald")
				|| path.contains("netherite")
				|| path.contains("ancient_debris")
				|| path.contains("gold")
				|| path.contains("iron")
				|| path.contains("lapis")
				|| path.contains("redstone")
				|| path.contains("blaze")
				|| path.contains("ender")
				|| path.equals("totem_of_undying")
				|| path.equals("enchanted_book")
				|| this.isSingleValuableItem(path);
	}

	private boolean isSingleValuableItem(String path) {
		return path.endsWith("_sword")
				|| path.endsWith("_pickaxe")
				|| path.endsWith("_axe")
				|| path.endsWith("_shovel")
				|| path.endsWith("_hoe")
				|| path.endsWith("_helmet")
				|| path.endsWith("_chestplate")
				|| path.endsWith("_leggings")
				|| path.endsWith("_boots")
				|| path.equals("bow")
				|| path.equals("crossbow")
				|| path.equals("trident")
				|| path.equals("mace")
				|| path.equals("shield")
				|| path.equals("elytra")
				|| path.equals("enchanted_book");
	}

	private BlockPos findTheftWanderPos(ServerLevel serverLevel) {
		if (this.theftTargetPos == null) {
			return null;
		}
		for (int radius = 2; radius <= 5; radius++) {
			for (int attempt = 0; attempt < 16; attempt++) {
				int x = this.random.nextInt(radius * 2 + 1) - radius;
				int z = this.random.nextInt(radius * 2 + 1) - radius;
				BlockPos origin = this.theftTargetPos.offset(x, 0, z);
				for (int yOffset = 2; yOffset >= -2; yOffset--) {
					BlockPos candidate = origin.offset(0, yOffset, 0);
					if (this.canStandAt(serverLevel, candidate)) {
						return candidate;
					}
				}
			}
		}
		return null;
	}

	private void clearTheftTaskState() {
		this.theftTaskMode = false;
		this.theftTargetPos = null;
		this.theftDoorPos = null;
		this.theftWanderPos = null;
		this.theftTaskTicks = 0;
		this.theftTaskPhase = 0;
		this.setNoAi(false);
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	private void clearTunnelDiggingState(ServerLevel serverLevel) {
		if (serverLevel != null && this.tunnelCurrentBlockPos != null) {
			serverLevel.destroyBlockProgress(this.getId(), this.tunnelCurrentBlockPos, -1);
		}
		this.tunnelDiggingMode = false;
		this.tunnelStartPos = null;
		this.tunnelCurrentBlockPos = null;
		this.tunnelDirection = null;
		this.tunnelLength = 0;
		this.tunnelBlockIndex = 0;
		this.tunnelBreakingTicks = 0;
		this.tunnelBreakingTotalTicks = 0;
		this.setNoAi(false);
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	private boolean canTunnelDigThrough(ServerLevel serverLevel, BlockPos pos, BlockState state) {
		if (state.liquid() || state.hasBlockEntity()) {
			return false;
		}
		return state.getDestroySpeed(serverLevel, pos) >= 0.0F;
	}

	private void faceBlock(BlockPos pos) {
		double dx = pos.getX() + 0.5D - this.getX();
		double dz = pos.getZ() + 0.5D - this.getZ();
		if (dx * dx + dz * dz < 0.0001D) {
			return;
		}
		float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
		this.setYRot(yaw);
		this.setYHeadRot(yaw);
		this.yBodyRot = yaw;
		this.yHeadRot = yaw;
		this.getLookControl().setLookAt(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	private Direction rightOf(Direction direction) {
		return switch (direction) {
			case NORTH -> Direction.EAST;
			case SOUTH -> Direction.WEST;
			case EAST -> Direction.SOUTH;
			case WEST -> Direction.NORTH;
			default -> Direction.EAST;
		};
	}

	private void faceStalkingPlayer(Player player) {
		double dx = player.getX() - this.getX();
		double dz = player.getZ() - this.getZ();
		if (dx * dx + dz * dz < 0.0001D) {
			return;
		}
		float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
		this.setYRot(yaw);
		this.setYHeadRot(yaw);
		this.yBodyRot = yaw;
		this.yHeadRot = yaw;
		this.getLookControl().setLookAt(player, 10.0F, 10.0F);
	}

	private boolean playerCaughtStalking(Player player) {
		if (this.distanceTo(player) < 10.0F) {
			return true;
		}
		if (this.distanceTo(player) > 48.0F || !this.hasLineOfSight(player)) {
			return false;
		}

		Vec3 playerEye = player.getEyePosition();
		Vec3 herobrineCenter = new Vec3(this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ());
		Vec3 toHerobrine = herobrineCenter.subtract(playerEye);
		if (toHerobrine.lengthSqr() < 0.0001D) {
			return true;
		}
		return player.getLookAngle().normalize().dot(toHerobrine.normalize()) > 0.965D;
	}

	private boolean tryAggressiveStalkingAttack(Player player) {
		if (!this.aggressiveStalking || this.stalkingReactionUsed || !(player instanceof ServerPlayer serverPlayer)) {
			return false;
		}
		this.stalkingReactionUsed = true;
		if (this.random.nextFloat() >= this.aggressiveStalkingAttackChance) {
			return false;
		}

		this.attackEnabled = true;
		this.aggressiveStalking = false;
		this.stalkingTicks = 0;
		this.setNoAi(false);
		this.setTarget(serverPlayer);
		this.startCombatWith(serverPlayer);
		return true;
	}

	private void startCombatWith(ServerPlayer player) {
		this.combatTargetId = player.getUUID();
		this.lightningStrikes = 0;
		this.lightningCooldown = 0;
		this.lightningWindowTicks = 0;
		this.consecutiveHandHits = 0;
		this.attackCooldown = 0;
		this.stuckTicks = 0;
		this.obstacleActionCooldown = 0;
		this.smartPlanCooldown = 0;
		this.directBreachTicks = 0;
		this.directBreachTntCooldown = 0;
		this.pendingBreachTntPos = null;
		this.pendingBreachTntTicks = 0;
		this.postKillCooldownTicks = 0;
		this.clearBreakingState(null);
		this.clearVerticalMovementCommit();
		this.clearDoorPlan();
		this.clearBuildColumnPlan();
		this.clearTunnelDiggingState(player.level());
		this.clearBlockTaskState(player.level());
		this.attackEnabled = true;
		this.setNoAi(false);
		this.aggressiveStalking = false;
		this.stalkingReactionUsed = false;
		this.stalkingSettled = false;
		this.stalkingWatchPos = null;
		this.stalkingRoamTarget = null;
		this.stalkingRoamCooldown = 0;
		this.aggressiveStalkingAttackChance = 0.0F;
		this.defensiveRetreatUsed = false;
		this.playerWasHoldingSword = this.isPlayerSword(player.getMainHandItem());
		this.defensiveSwordCheckResolved = this.playerWasHoldingSword;
		this.combatTicks = 0;
		this.revengeDamageProgress = 0.0F;
		this.revengeStepIndex = 0;
		this.pendingRevengeStage = 0;
		this.revengeComboTicks = 0;
		this.revengeComboStage = 0;
		this.revengeTargetId = null;
		this.lastCombatX = this.getX();
		this.lastCombatZ = this.getZ();
		this.chooseCombatWeapon();
	}

	private void resetCombat() {
		this.combatTargetId = null;
		this.lightningStrikes = 0;
		this.lightningCooldown = 0;
		this.lightningWindowTicks = 0;
		this.consecutiveHandHits = 0;
		this.attackCooldown = 0;
		this.stuckTicks = 0;
		this.obstacleActionCooldown = 0;
		this.smartPlanCooldown = 0;
		this.directBreachTicks = 0;
		this.directBreachTntCooldown = 0;
		this.pendingBreachTntPos = null;
		this.pendingBreachTntTicks = 0;
		this.weaponRerollTicks = 0;
		this.clearBreakingState(null);
		this.clearVerticalMovementCommit();
		this.clearDoorPlan();
		this.clearBuildColumnPlan();
		if (this.level() instanceof ServerLevel serverLevel) {
			this.clearTunnelDiggingState(serverLevel);
			this.clearBlockTaskState(serverLevel);
		} else {
			this.clearTunnelDiggingState(null);
			this.clearBlockTaskState(null);
		}
		this.usingUtilityTool = false;
		this.attackEnabled = true;
		this.setNoAi(false);
		this.stalkingTicks = 0;
		this.stalkingVanishCue = false;
		this.stalkingSettled = false;
		this.stalkingWatchPos = null;
		this.stalkingRoamTarget = null;
		this.stalkingTravelTicks = 0;
		this.stalkingRoamCooldown = 0;
		this.stalkingCoverStep = 0;
		this.stalkingCoverCooldown = 0;
		this.aggressiveStalking = false;
		this.stalkingReactionUsed = false;
		this.aggressiveStalkingAttackChance = 0.0F;
		this.defensiveRetreatUsed = false;
		this.playerWasHoldingSword = false;
		this.defensiveSwordCheckResolved = false;
		this.combatTicks = 0;
		this.revengeDamageProgress = 0.0F;
		this.revengeStepIndex = 0;
		this.pendingRevengeStage = 0;
		this.revengeComboTicks = 0;
		this.revengeComboStage = 0;
		this.revengeTargetId = null;
		this.revengeKnockbackDone = false;
		this.revengeTeleportDone = false;
		this.revengeFireballDone = false;
		this.revengeArmorDropDone = false;
		this.revengeLightningDone = false;
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	private void trackRevengeDamage(float amount, ServerPlayer attackingPlayer, ServerLevel serverLevel) {
		if (this.deathLootTicks > 0 || this.mythicHealth <= VANISH_HEALTH_POINTS) {
			return;
		}
		if (this.combatTargetId == null || !attackingPlayer.getUUID().equals(this.combatTargetId)) {
			this.setTarget(attackingPlayer);
			this.startCombatWith(attackingPlayer);
		}

		this.revengeDamageProgress += amount;
		while (this.revengeDamageProgress >= 130.0F) {
			this.revengeDamageProgress -= 130.0F;
			this.revengeStepIndex++;
			this.pendingRevengeStage = 1;
		}
		if (this.revengeComboTicks <= 0 && this.getTarget() == null) {
			this.setTarget(attackingPlayer);
		}
	}

	private float getCurrentRevengeStepPoints() {
		return 150.0F;
	}

	private boolean tickRevengeCombo(ServerPlayer player, ServerLevel serverLevel) {
		if (this.revengeComboTicks <= 0 && this.pendingRevengeStage > 0) {
			this.startRevengeCombo(player, serverLevel, this.pendingRevengeStage);
		}
		if (this.revengeComboTicks <= 0) {
			return false;
		}
		if (player.isDeadOrDying() || !player.isAlive() || player.getHealth() <= 0.0F) {
			this.handlePlayerDeath(player);
			return true;
		}

		this.setNoAi(true);
		this.getNavigation().stop();
		this.setTarget(player);
		this.getLookControl().setLookAt(player, 30.0F, 30.0F);

		int elapsed = REVENGE_TOTAL_TICKS - this.revengeComboTicks;
		this.applyRevengeMovementLock(player, elapsed);
		if (!this.revengeKnockbackDone && elapsed >= REVENGE_KNOCKBACK_TICK) {
			this.applyRevengeKnockback(player, serverLevel);
			this.revengeKnockbackDone = true;
		}
		if (!this.revengeTeleportDone && elapsed >= REVENGE_TELEPORT_TICK) {
			this.tryRevengeTeleportInFront(player, serverLevel);
			this.revengeTeleportDone = true;
		}
		if (!this.revengeFireballDone && elapsed >= REVENGE_FIREBALL_TICK) {
			this.launchRevengeFireball(player, serverLevel);
			this.revengeFireballDone = true;
		}
		if (!this.revengeLightningDone && elapsed >= REVENGE_LIGHTNING_TICK) {
			this.strikeRevengeLightning(player, serverLevel);
			this.revengeLightningDone = true;
		}

		this.revengeComboTicks--;
		if (this.revengeComboTicks <= 0) {
			this.finishRevengeCombo(player);
		}
		return true;
	}

	private void startRevengeCombo(ServerPlayer player, ServerLevel serverLevel, int stage) {
		this.pendingRevengeStage = 0;
		this.revengeComboStage = Math.max(1, Math.min(3, stage));
		this.revengeComboTicks = REVENGE_TOTAL_TICKS;
		this.revengeTargetId = player.getUUID();
		this.revengeKnockbackDone = false;
		this.revengeTeleportDone = false;
		this.revengeFireballDone = false;
		this.revengeArmorDropDone = false;
		this.revengeLightningDone = false;
		this.clearBreakingState(serverLevel);
		this.clearDoorPlan();
		this.clearBuildColumnPlan();
		this.clearVerticalMovementCommit();
		this.pendingBreachTntPos = null;
		this.pendingBreachTntTicks = 0;
		this.usingUtilityTool = false;
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		this.setNoAi(true);
		this.getNavigation().stop();
		this.setTarget(player);
	}

	private void applyRevengeKnockback(ServerPlayer player, ServerLevel serverLevel) {
		// برق بقلب واحد + ضرر في الأرض + knockback
		LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
		if (lightningBolt != null) {
			lightningBolt.setVisualOnly(true);
			lightningBolt.setCause(player);
			lightningBolt.snapTo(player.getX(), player.getY(), player.getZ());
			serverLevel.addFreshEntity(lightningBolt);
		}
		serverLevel.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.1F, 0.9F);
		serverLevel.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.1D);
		player.hurtServer(serverLevel, serverLevel.damageSources().mobAttack(this), 1.0F);
		// knockback بعد البرق
		Vec3 direction = player.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
		if (direction.lengthSqr() < 0.0001D) {
			direction = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).reverse();
		}
		Vec3 push = direction.normalize().scale(REVENGE_KNOCKBACK_POWER);
		player.push(push.x, 0.55D, push.z);
		player.hurtMarked = true;
		serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0D, player.getZ(), 18, 0.45D, 0.65D, 0.45D, 0.08D);
	}

	private void applyRevengeMovementLock(ServerPlayer player, int elapsed) {
		if (elapsed < REVENGE_KNOCKBACK_TICK + 8 || elapsed > REVENGE_LIGHTNING_TICK + 10) {
			return;
		}
		player.setSprinting(false);
		Vec3 velocity = player.getDeltaMovement();
		double horizontalSqr = velocity.x * velocity.x + velocity.z * velocity.z;
		if (horizontalSqr <= REVENGE_STUN_HORIZONTAL_LIMIT * REVENGE_STUN_HORIZONTAL_LIMIT) {
			return;
		}
		double scale = REVENGE_STUN_HORIZONTAL_LIMIT / Math.sqrt(horizontalSqr);
		player.setDeltaMovement(velocity.x * scale, velocity.y, velocity.z * scale);
		player.hurtMarked = true;
	}

	private void triggerPortalDistortion(ServerLevel serverLevel, ServerPlayer player) {
		player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 110, 1, false, false), this);
		if (ServerPlayNetworking.canSend(player, HerobrineVisualEffectPayload.TYPE)) {
			ServerPlayNetworking.send(player, new HerobrineVisualEffectPayload(110, 0.35F));
		}
		serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 58, 0.65D, 0.75D, 0.65D, 0.1D);
	}

	private boolean tryRevengeTeleportInFront(ServerPlayer player, ServerLevel serverLevel) {
		Vec3 look = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
		if (look.lengthSqr() < 0.0001D) {
			look = new Vec3(0.0D, 0.0D, 1.0D);
		}
		look = look.normalize();
		Vec3 target = player.position().add(look.scale(REVENGE_TELEPORT_DISTANCE));
		BlockPos origin = BlockPos.containing(target.x, player.getY(), target.z);

		for (int yOffset = 3; yOffset >= -5; yOffset--) {
			BlockPos candidate = origin.offset(0, yOffset, 0);
			if (this.canSafeCombatTeleportAt(serverLevel, candidate)) {
				serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.35D, 0.7D, 0.35D, 0.035D);
				this.teleportTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D);
				serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.35D, 0.7D, 0.35D, 0.035D);
				serverLevel.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.85F, 0.85F);
				this.getLookControl().setLookAt(player, 30.0F, 30.0F);
				return true;
			}
		}
		return this.tryTacticalTeleport(player, serverLevel);
	}

	private void launchRevengeFireball(ServerPlayer player, ServerLevel serverLevel) {
		Vec3 start = this.getEyePosition().add(this.getLookAngle().normalize().scale(0.8D));
		Vec3 aim = player.getEyePosition().subtract(start);
		if (aim.lengthSqr() < 0.0001D) {
			aim = player.position().subtract(this.position());
		}
		Vec3 direction = aim.normalize();
		this.swing(InteractionHand.MAIN_HAND, true);
		serverLevel.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 0.65F);
		// مسار بصري بس من غير entity حقيقي عشان مايعملش دامج زيادة من التصادم
		for (double t = 0.1D; t <= 1.0D; t += 0.1D) {
			Vec3 p = start.add(aim.scale(t));
			serverLevel.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 3, 0.05D, 0.05D, 0.05D, 0.01D);
		}
		serverLevel.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1.0D, player.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
		serverLevel.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.0F, 1.0F);
		player.hurtServer(serverLevel, serverLevel.damageSources().mobAttack(this), this.getRevengeFireballDamage(player));
		// نحد الـ knockback عشان اللاعب ميطيرش أوي، بلوكة بس
		Vec3 vel = player.getDeltaMovement();
		double maxH = 0.4D;
		double maxV = 0.42D;
		player.setDeltaMovement(
			Math.max(-maxH, Math.min(maxH, vel.x)),
			Math.min(maxV, vel.y),
			Math.max(-maxH, Math.min(maxH, vel.z))
		);
		player.hurtMarked = true;
		if (player.isDeadOrDying() || !player.isAlive() || player.getHealth() <= 0.0F) {
			this.handlePlayerDeath(player);
		}
	}

	private float getRevengeFireballDamage() {
		return 3.0F;
	}

	private float getRevengeFireballDamage(ServerPlayer player) {
		return 4.0F;
	}

	private float getRevengeLightningDamage() {
		return 5.0F;
	}

	private float getRevengeLightningDamage(ServerPlayer player) {
		return 16.0F;
	}

	private int countArmorPieces(ServerPlayer player) {
		int count = 0;
		for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
			net.minecraft.world.entity.EquipmentSlot.HEAD,
			net.minecraft.world.entity.EquipmentSlot.CHEST,
			net.minecraft.world.entity.EquipmentSlot.LEGS,
			net.minecraft.world.entity.EquipmentSlot.FEET
		}) {
			if (!player.getItemBySlot(slot).isEmpty()) count++;
		}
		return count;
	}

	private String getArmorTier(ServerPlayer player) {
		int netheriteCount = 0, diamondCount = 0, ironCount = 0, chainCount = 0, goldenCount = 0, copperCount = 0, leatherCount = 0;
		for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
			net.minecraft.world.entity.EquipmentSlot.HEAD,
			net.minecraft.world.entity.EquipmentSlot.CHEST,
			net.minecraft.world.entity.EquipmentSlot.LEGS,
			net.minecraft.world.entity.EquipmentSlot.FEET
		}) {
			net.minecraft.world.item.ItemStack item = player.getItemBySlot(slot);
			if (item.isEmpty()) continue;
			String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem()).getPath();
			if (id.startsWith("netherite"))   netheriteCount++;
			else if (id.startsWith("diamond")) diamondCount++;
			else if (id.startsWith("iron"))    ironCount++;
			else if (id.startsWith("chainmail")) chainCount++;
			else if (id.startsWith("golden"))  goldenCount++;
			else if (id.startsWith("copper"))  copperCount++;
			else if (id.startsWith("leather") || id.startsWith("turtle")) leatherCount++;
		}
		if (netheriteCount > 0 && netheriteCount >= diamondCount) return "netherite";
		if (diamondCount > 0 && diamondCount >= ironCount) return "diamond";
		if (ironCount > 0 && ironCount >= chainCount) return "iron";
		if (chainCount > 0 && chainCount >= goldenCount) return "chainmail";
		if (goldenCount > 0 && goldenCount >= copperCount) return "golden";
		if (copperCount > 0 && copperCount >= leatherCount) return "copper";
		if (leatherCount > 0) return "leather";
		return "none";
	}

	private float calcArmorDamage(ServerPlayer player, float noDmg, float leatherFull, float ironFull, float diamondFull, float netheriteFull) {
		String tier = this.getArmorTier(player);
		int pieces = this.countArmorPieces(player);
		float fullDmg;
		switch (tier) {
			case "leather"   -> fullDmg = leatherFull;
			case "copper"    -> fullDmg = leatherFull + (ironFull - leatherFull) * (2.0F / 4.0F);
			case "golden"    -> fullDmg = leatherFull + (ironFull - leatherFull) * (2.5F / 4.0F);
			case "chainmail" -> fullDmg = leatherFull + (ironFull - leatherFull) * (3.0F / 4.0F);
			case "iron"      -> fullDmg = ironFull;
			case "diamond"   -> fullDmg = diamondFull;
			case "netherite" -> fullDmg = netheriteFull;
			default          -> { return noDmg; }
		}
		float missingPieces = 4 - pieces;
		float step = (noDmg - fullDmg) / 4.0F;
		return fullDmg + step * missingPieces;
	}

	private float calcWeaponDamage(ServerPlayer player) {
		ItemStack weapon = this.getMainHandItem();
		if (weapon.isEmpty()) return 5.0F;
		String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(weapon.getItem()).getPath();
		if (id.equals("diamond_sword")) return 8.0F;
		if (id.equals("golden_sword")) return 6.0F;
		if (id.equals("golden_pickaxe")) return 5.0F;
		if (id.equals("diamond_pickaxe")) return 6.0F;
		return 5.0F;
	}

	private void dropRevengeArmor(ServerPlayer player, ServerLevel serverLevel) {
		int minCount = this.revengeComboStage >= 3 ? 2 : this.revengeComboStage;
		int maxCount = this.revengeComboStage >= 3 ? 3 : this.revengeComboStage;
		this.dropRandomPlayerArmor(player, serverLevel, minCount, maxCount);
	}

	private void strikeRevengeLightning(ServerPlayer player, ServerLevel serverLevel) {
		this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BLAZE_ROD));
		this.swing(InteractionHand.MAIN_HAND, true);

		LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
		if (lightningBolt != null) {
			lightningBolt.setVisualOnly(true);
			lightningBolt.setCause(player);
			lightningBolt.snapTo(player.getX(), player.getY(), player.getZ());
			serverLevel.addFreshEntity(lightningBolt);
		}

		serverLevel.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.1F, 0.9F);
		serverLevel.playSound(null, player.blockPosition(), ModSounds.CAVE_7, SoundSource.HOSTILE, 1.2F, 1.0F);
		// انفجار حقيقي يكسر بلوكات ويعمل نار، اللاعب محمي مؤقتاً
		boolean wasInvulnerable = player.isInvulnerable();
		player.setInvulnerable(true);
		serverLevel.explode(this, player.getX(), player.getY(), player.getZ(), REVENGE_LIGHTNING_GROUND_POWER, true, Level.ExplosionInteraction.BLOCK);
		player.setInvulnerable(wasInvulnerable);
		player.hurtServer(serverLevel, serverLevel.damageSources().mobAttack(this), this.getRevengeLightningDamage(player));
		if (player.isDeadOrDying() || !player.isAlive() || player.getHealth() <= 0.0F) {
			this.handlePlayerDeath(player);
		}
	}

	private void finishRevengeCombo(ServerPlayer player) {
		this.revengeComboStage = 0;
		this.revengeTargetId = null;
		this.revengeKnockbackDone = false;
		this.revengeTeleportDone = false;
		this.revengeFireballDone = false;
		this.revengeArmorDropDone = false;
		this.revengeLightningDone = false;
		this.setNoAi(false);
		this.setTarget(player);
		this.attackCooldown = Math.max(this.attackCooldown, 10);
		this.chooseCombatWeapon();
	}

	private boolean hasIronOrBetterArmor(ServerPlayer player) {
		int strongPieces = 0;
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			if (this.isIronOrBetterArmor(player.getItemBySlot(slot))) {
				strongPieces++;
			}
		}
		return strongPieces >= 2;
	}

	private boolean isIronOrBetterArmor(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		return stack.is(Items.IRON_HELMET) || stack.is(Items.IRON_CHESTPLATE) || stack.is(Items.IRON_LEGGINGS) || stack.is(Items.IRON_BOOTS)
				|| stack.is(Items.DIAMOND_HELMET) || stack.is(Items.DIAMOND_CHESTPLATE) || stack.is(Items.DIAMOND_LEGGINGS) || stack.is(Items.DIAMOND_BOOTS)
				|| stack.is(Items.NETHERITE_HELMET) || stack.is(Items.NETHERITE_CHESTPLATE) || stack.is(Items.NETHERITE_LEGGINGS) || stack.is(Items.NETHERITE_BOOTS);
	}

	private void applyReducedNaturalKnockback(DamageSource damageSource) {
		Entity attacker = damageSource.getEntity();
		if (attacker == null) {
			attacker = damageSource.getDirectEntity();
		}
		if (attacker == null) {
			return;
		}

		Vec3 away = this.position().subtract(attacker.position()).multiply(1.0D, 0.0D, 1.0D);
		if (away.lengthSqr() < 0.0001D) {
			return;
		}
		Vec3 push = away.normalize().scale(0.32D);
		this.push(push.x, 0.12D, push.z);
		this.hurtMarked = true;
	}

	private void dropPlayerArmor(ServerPlayer player, ServerLevel serverLevel) {
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			this.dropArmorSlot(player, slot);
		}

		serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0D, player.getZ(), 20, 0.5D, 0.7D, 0.5D, 0.1D);
		serverLevel.playSound(null, player.blockPosition(), ModSounds.CAVE_10, SoundSource.HOSTILE, 1.8F, 1.0F);
	}

	private void dropRandomPlayerArmor(ServerPlayer player, ServerLevel serverLevel, int minCount, int maxCount) {
		List<EquipmentSlot> wornSlots = new ArrayList<>();
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			if (!player.getItemBySlot(slot).isEmpty()) {
				wornSlots.add(slot);
			}
		}
		if (wornSlots.isEmpty()) {
			return;
		}

		int count = Math.min(wornSlots.size(), minCount + this.random.nextInt(Math.max(1, maxCount - minCount + 1)));
		for (int i = 0; i < count && !wornSlots.isEmpty(); i++) {
			EquipmentSlot slot = wornSlots.remove(this.random.nextInt(wornSlots.size()));
			this.dropArmorSlot(player, slot);
		}

		serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0D, player.getZ(), 12 + count * 4, 0.5D, 0.7D, 0.5D, 0.1D);
		serverLevel.playSound(null, player.blockPosition(), ModSounds.CAVE_10, SoundSource.HOSTILE, 1.45F, 1.0F);
	}

	private void dropArmorSlot(ServerPlayer player, EquipmentSlot slot) {
		ItemStack armor = player.getItemBySlot(slot);
		if (armor.isEmpty()) {
			return;
		}
		ItemStack droppedArmor = armor.copy();
		player.setItemSlot(slot, ItemStack.EMPTY);
		player.drop(droppedArmor, true);
	}

	private void triggerBombVolleyIfNeeded(ServerLevel serverLevel) {
		if (this.mythicHealth > this.nextBombHealthThreshold || this.nextBombHealthThreshold < VANISH_HEALTH_POINTS) {
			return;
		}

		this.throwBombVolley(serverLevel);
		do {
			this.nextBombHealthThreshold -= BOMB_HEALTH_STEP_POINTS;
		} while (this.mythicHealth <= this.nextBombHealthThreshold && this.nextBombHealthThreshold >= VANISH_HEALTH_POINTS);
	}

	private void throwBombVolley(ServerLevel serverLevel) {
		Entity target = this.getTarget();
		Vec3 center = target != null ? target.position() : this.position();
		serverLevel.playSound(null, BlockPos.containing(center), ModSounds.CAVE_9, SoundSource.HOSTILE, 1.3F, 1.0F);
		for (int i = 0; i < 3; i++) {
			double angle = this.random.nextDouble() * Math.PI * 2.0D;
			double radius = 1.25D + this.random.nextDouble() * 1.75D;
			double x = center.x + Math.cos(angle) * radius;
			double z = center.z + Math.sin(angle) * radius;
			serverLevel.explode(this, x, center.y + 0.15D, z, BOMB_EXPLOSION_POWER, false, Level.ExplosionInteraction.NONE);
		}
	}

	private void chooseCombatWeapon() {
		int roll = this.random.nextInt(100);
		ItemStack weapon = ItemStack.EMPTY;
		if (roll >= 65 && roll < 74) {
			weapon = new ItemStack(Items.DIAMOND_SWORD);
		} else if (roll >= 74 && roll < 83) {
			weapon = new ItemStack(Items.GOLDEN_SWORD);
		} else if (roll >= 83 && roll < 91) {
			weapon = new ItemStack(Items.GOLDEN_PICKAXE);
		} else if (roll >= 91) {
			weapon = new ItemStack(Items.DIAMOND_PICKAXE);
		}

		this.setItemInHand(InteractionHand.MAIN_HAND, weapon);
		this.usingUtilityTool = false;
		this.weaponRerollTicks = WEAPON_REROLL_TICKS + this.random.nextInt(WEAPON_REROLL_TICKS);
	}

	private boolean tryDefensiveSwordRetreat(ServerPlayer player, ServerLevel serverLevel) {
		ItemStack hand = player.getMainHandItem();
		boolean holdingSword = this.isPlayerSword(hand);
		if (this.defensiveSwordCheckResolved) {
			this.playerWasHoldingSword = holdingSword;
			return false;
		}
		if (this.combatTicks > DEFENSIVE_RETREAT_WINDOW_TICKS) {
			this.defensiveSwordCheckResolved = true;
			this.playerWasHoldingSword = holdingSword;
			return false;
		}
		if (!this.defensiveRetreatUsed && !this.playerWasHoldingSword && holdingSword) {
			this.defensiveRetreatUsed = true;
			this.defensiveSwordCheckResolved = true;
			this.playerWasHoldingSword = true;
			return this.tryFarDefensiveRetreat(player, serverLevel);
		}

		this.playerWasHoldingSword = holdingSword;
		return false;
	}

	private boolean isPlayerSword(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		return stack.is(Items.WOODEN_SWORD) || stack.is(Items.STONE_SWORD) || stack.is(Items.COPPER_SWORD)
				|| stack.is(Items.GOLDEN_SWORD) || stack.is(Items.IRON_SWORD) || stack.is(Items.DIAMOND_SWORD) || stack.is(Items.NETHERITE_SWORD);
	}

	private boolean tryFarDefensiveRetreat(ServerPlayer player, ServerLevel serverLevel) {
		serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 50, 0.35D, 0.75D, 0.35D, 0.04D);
		serverLevel.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8F, 0.75F);

		Vec3 look = player.getLookAngle();
		Vec3 flatLook = new Vec3(look.x, 0.0D, look.z);
		if (flatLook.lengthSqr() < 0.001D) {
			flatLook = new Vec3(0.0D, 0.0D, 1.0D);
		}
		flatLook = flatLook.normalize();
		Vec3 side = new Vec3(-flatLook.z, 0.0D, flatLook.x);

		for (int attempt = 0; attempt < 36; attempt++) {
			double sideOffset = ((attempt % 7) - 3) * 1.75D + (this.random.nextDouble() - 0.5D) * 1.5D;
			double backOffset = DEFENSIVE_RETREAT_MIN_DISTANCE + this.random.nextDouble() * DEFENSIVE_RETREAT_EXTRA_DISTANCE;
			double x = player.getX() - flatLook.x * backOffset + side.x * sideOffset;
			double z = player.getZ() - flatLook.z * backOffset + side.z * sideOffset;
			BlockPos origin = BlockPos.containing(x, player.getY(), z);

			for (int yOffset = 6; yOffset >= -8; yOffset--) {
				BlockPos candidate = origin.offset(0, yOffset, 0);
				if (this.canSafeCombatTeleportAt(serverLevel, candidate)) {
					this.clearBreakingState(serverLevel);
					this.clearDoorPlan();
					this.clearBuildColumnPlan();
					this.clearVerticalMovementCommit();
					this.teleportTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D);
					serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 50, 0.35D, 0.75D, 0.35D, 0.04D);
					serverLevel.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8F, 0.75F);
					this.setTarget(player);
					this.getLookControl().setLookAt(player, 30.0F, 30.0F);
					this.getNavigation().moveTo(player, 1.08D);
					return true;
				}
			}
		}

		return this.tryTacticalTeleport(player, serverLevel);
	}

	private void handlePlayerDeath(ServerPlayer player) {
		if (this.deathLootTicks > 0) {
			return;
		}
		HerobrineSpawnManager.resetCycleAfterDeathScene(player);
		if (this.level() instanceof ServerLevel serverLevel) {
			this.startDeathLootScene(player, serverLevel);
		}
	}

	public boolean isCollectingDeathLootFor(ServerPlayer player) {
		return this.deathLootTicks > 0 && this.deathLootTargetId != null && this.deathLootTargetId.equals(player.getUUID());
	}

	public void finishDeathLootAfterRespawn() {
		if (this.deathLootTicks > 0 && this.level() instanceof ServerLevel serverLevel) {
			this.finishDeathLootScene(serverLevel);
		}
	}

	private void tickTrackedDeathTarget(ServerLevel serverLevel) {
		if (this.deathLootTicks > 0 || this.combatTargetId == null) {
			return;
		}
		ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(this.combatTargetId);
		if (player != null && this.isDeadPlayer(player)) {
			this.handlePlayerDeath(player);
		}
	}

	private boolean isDeadPlayer(ServerPlayer player) {
		return player.isDeadOrDying() || !player.isAlive() || player.getHealth() <= 0.0F;
	}

	private boolean hasRespawnedAfterDeath(ServerPlayer player) {
		return player.isAlive() && !player.isDeadOrDying() && player.getHealth() > 0.0F;
	}

	private void startDeathLootScene(ServerPlayer player, ServerLevel serverLevel) {
		this.deathLootTargetId = player.getUUID();
		this.deathLootPos = player.blockPosition().immutable();
		this.deathLootTicks = DEATH_LOOT_SCENE_TICKS;
		this.deathLootPickupCooldown = DEATH_LOOT_FIRST_PICKUP_TICKS;
		this.revengeComboTicks = 0;
		this.pendingRevengeStage = 0;
		this.setTarget(null);
		this.getNavigation().stop();
		this.clearBreakingState(serverLevel);
		this.clearDoorPlan();
		this.clearBuildColumnPlan();
		this.clearVerticalMovementCommit();
		this.pendingBreachTntPos = null;
		this.pendingBreachTntTicks = 0;
		this.attackEnabled = false;
		this.setNoAi(false);
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		if (this.distanceToSqr(player) > 64.0D) {
			this.tryTeleportNearDeathLoot(serverLevel, this.deathLootPos);
		}
		serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 20, 0.25D, 0.45D, 0.25D, 0.02D);
	}

	private void tickDeathLootScene(ServerLevel serverLevel) {
		if (this.deathLootPos == null) {
			this.finishDeathLootScene(serverLevel);
			return;
		}
		this.setTarget(null);
		this.attackEnabled = false;
		this.setNoAi(false);
		this.moveTowardDeathLoot(serverLevel);

		Player player = null;
		if (this.deathLootTargetId != null) {
			player = serverLevel.getPlayerInAnyDimension(this.deathLootTargetId);
		}
		if (player != null) {
			this.getLookControl().setLookAt(player, 30.0F, 30.0F);
			if (player instanceof ServerPlayer serverPlayer && this.hasRespawnedAfterDeath(serverPlayer)) {
				this.pickupDeathLoot(serverLevel);
				this.finishDeathLootScene(serverLevel);
				return;
			}
		} else {
			this.getLookControl().setLookAt(this.deathLootPos.getX() + 0.5D, this.deathLootPos.getY() + 0.4D, this.deathLootPos.getZ() + 0.5D, 30.0F, 30.0F);
		}

		if (this.deathLootPickupCooldown > 0) {
			this.deathLootPickupCooldown--;
		} else {
			this.pickupDeathLoot(serverLevel);
			this.deathLootPickupCooldown = DEATH_LOOT_PICKUP_INTERVAL_TICKS;
		}

		this.deathLootTicks--;
		if (this.deathLootTicks <= 0) {
			this.finishDeathLootScene(serverLevel);
		}
	}

	private void moveTowardDeathLoot(ServerLevel serverLevel) {
		double targetX = this.deathLootPos.getX() + 0.5D;
		double targetY = this.deathLootPos.getY();
		double targetZ = this.deathLootPos.getZ() + 0.5D;
		double distanceSqr = this.distanceToSqr(targetX, targetY, targetZ);
		if (distanceSqr > 100.0D && this.tryTeleportNearDeathLoot(serverLevel, this.deathLootPos)) {
			return;
		}
		if (distanceSqr > 3.0D) {
			this.getNavigation().moveTo(targetX, targetY, targetZ, 0.86D);
		} else {
			this.getNavigation().stop();
		}
	}

	private void pickupDeathLoot(ServerLevel serverLevel) {
		if (serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY)) {
			return;
		}
		if (this.deathLootPos == null) {
			return;
		}

		AABB lootArea = new AABB(this.deathLootPos).inflate(10.0D, 6.0D, 10.0D);
		List<ItemEntity> items = serverLevel.getEntitiesOfClass(ItemEntity.class, lootArea, item -> item.isAlive() && !item.getItem().isEmpty());
		// بناخد 30% بس من الحاجات
		int toTake = Math.max(1, (int) Math.ceil(items.size() * 0.30));
		// نخلط الـ list عشان الـ 30% تكون عشوائية مش الأوائل بس
		java.util.Collections.shuffle(items, new java.util.Random(this.random.nextLong()));
		int taken = 0;
		for (int i = 0; i < toTake && i < items.size(); i++) {
			ItemEntity item = items.get(i);
			serverLevel.sendParticles(ParticleTypes.SMOKE, item.getX(), item.getY() + 0.2D, item.getZ(), 12, 0.16D, 0.18D, 0.16D, 0.02D);
			item.discard();
			taken++;
		}
		if (taken > 0) {
			this.swing(InteractionHand.MAIN_HAND, true);
			serverLevel.playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.HOSTILE, 0.85F, 0.65F);
		}
	}

	private boolean tryTeleportNearDeathLoot(ServerLevel serverLevel, BlockPos deathPos) {
		for (int radius = 1; radius <= 4; radius++) {
			for (int attempt = 0; attempt < 14; attempt++) {
				int x = this.random.nextInt(radius * 2 + 1) - radius;
				int z = this.random.nextInt(radius * 2 + 1) - radius;
				BlockPos origin = deathPos.offset(x, 0, z);
				for (int yOffset = 3; yOffset >= -5; yOffset--) {
					BlockPos candidate = origin.offset(0, yOffset, 0);
					if (this.canStandAt(serverLevel, candidate)) {
						this.teleportTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D);
						serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 24, 0.28D, 0.5D, 0.28D, 0.025D);
						return true;
					}
				}
			}
		}
		return false;
	}

	private void finishDeathLootScene(ServerLevel serverLevel) {
		ServerPlayer player = this.deathLootTargetId == null ? null : serverLevel.getServer().getPlayerList().getPlayer(this.deathLootTargetId);
		BlockPos soundPos = this.blockPosition();
		this.pickupDeathLoot(serverLevel);
		this.deathLootTargetId = null;
		this.deathLootPos = null;
		this.deathLootTicks = 0;
		this.deathLootPickupCooldown = 0;
		this.clearBreakingState(serverLevel);
		this.clearTunnelDiggingState(serverLevel);
		this.clearBlockTaskState(serverLevel);
		serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 60, 0.4D, 0.8D, 0.4D, 0.04D);
		serverLevel.playSound(null, soundPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.75F);
		serverLevel.playSound(null, soundPos, ModSounds.CAVE_2, SoundSource.MASTER, 2.0F, 1.0F);
		if (player != null) {
			serverLevel.playSound(null, player.blockPosition(), ModSounds.CAVE_2, SoundSource.MASTER, 2.25F, 1.0F);
		}
		this.discard();
	}

	public void moveTowardCombatTarget(LivingEntity target) {
		if (target instanceof ServerPlayer player && this.level() instanceof ServerLevel serverLevel) {
			this.tickWaterPursuit(player);
			if (this.isWaterPursuit(player)) {
				this.getNavigation().moveTo(target, 1.02D);
				return;
			}
			if (this.tickSmartCombatPlanner(player, serverLevel)) {
				return;
			}
			if (this.tickCommittedVerticalMovement(player, serverLevel)) {
				return;
			}
			this.getNavigation().moveTo(target, this.isWaterPursuit(player) ? 1.12D : 1.08D);
			return;
		}
		this.getNavigation().moveTo(target, 1.08D);
	}

	public boolean isBusyWithManualCombatTask() {
		return this.breakingBlockPos != null
				|| this.pendingBreachTntPos != null
				|| this.activeDoorPlanPos != null
				|| this.activeClimbPos != null
				|| this.activeVerticalRoutePos != null
				|| this.activeBuildColumnPos != null;
	}

	private void tickWaterPursuit(ServerPlayer player) {
		if (!this.isWaterPursuit(player)) {
			return;
		}

		this.clearBuildColumnPlan();
		this.getNavigation().moveTo(player, 1.02D);
		if (!this.isInWater()) {
			return;
		}

		Vec3 toPlayer = player.position().subtract(this.position());
		Vec3 horizontal = new Vec3(toPlayer.x, 0.0D, toPlayer.z);
		Vec3 current = this.getDeltaMovement();

		// لو في أرض صلبة قدامه على ارتفاع بلوك واحد فوقه، نسيبه يطلع عليها طبيعي (زي اللاعب) من غير ما نفرض ارتفاع المية
		net.minecraft.core.BlockPos myPos = this.blockPosition();
		Vec3 lookDir = horizontal.lengthSqr() > 0.01D ? horizontal.normalize() : this.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
		net.minecraft.core.BlockPos aheadPos = myPos.offset((int) Math.round(lookDir.x), 0, (int) Math.round(lookDir.z));
		boolean edgeAhead = this.level().getBlockState(aheadPos).isSolid()
			&& this.level().getBlockState(aheadPos.above()).getFluidState().isEmpty();
		if (edgeAhead) {
			this.setDeltaMovement(current.x * 0.7D, current.y, current.z * 0.7D);
			return;
		}

		// نحدد مستوى سطح المية فوق الـ entity ونعومه زي اللاعب: نص جسمه فوق ونص تحت
		net.minecraft.core.BlockPos basePos = myPos;
		double surfaceY = basePos.getY() + 1.0D;
		for (int i = 0; i < 4; i++) {
			net.minecraft.core.BlockPos checkPos = basePos.above(i);
			if (!this.level().getFluidState(checkPos).isSource() && this.level().getFluidState(checkPos).isEmpty()) {
				surfaceY = checkPos.getY();
				break;
			}
			surfaceY = checkPos.getY() + 1.0D;
		}
		// اللاعب وقت العوم عينه عند السطح، يعني جسمه (الأسفل) يبقى تحت السطح بمقدار نص الـ hitbox تقريباً
		double targetY = surfaceY - (this.getBbHeight() * 0.55D);
		double yMove = this.clamp((targetY - this.getY()) * 0.08D, -0.05D, 0.05D);
		double xBoost = 0.0D;
		double zBoost = 0.0D;
		if (horizontal.lengthSqr() > 0.01D) {
			Vec3 push = horizontal.normalize().scale(0.026D);
			xBoost = push.x;
			zBoost = push.z;
		}
		this.setDeltaMovement(current.x * 0.7D + xBoost, current.y * 0.55D + yMove, current.z * 0.7D + zBoost);
		this.setSwimming(true);
	}

	private boolean isWaterPursuit(ServerPlayer player) {
		return this.isInWater() || player.isInWater();
	}

	private boolean tickCommittedVerticalMovement(ServerPlayer player, ServerLevel serverLevel) {
		if (this.activeClimbPos != null && this.tickActiveClimb(player, serverLevel)) {
			return true;
		}
		if (this.activeVerticalRoutePos != null && this.tickActiveVerticalRoute(player, serverLevel)) {
			return true;
		}
		return false;
	}

	private boolean tickActiveClimb(ServerPlayer player, ServerLevel serverLevel) {
		if (this.activeClimbPos == null) {
			return false;
		}
		if (this.activeClimbTicks-- <= 0 || player.getY() <= this.getY() + 0.6D) {
			this.clearVerticalMovementCommit();
			return false;
		}

		BlockPos climbPos = this.findClosestClimbBlockInCommittedColumn(serverLevel);
		if (climbPos == null) {
			this.clearVerticalMovementCommit();
			return false;
		}
		this.activeClimbPos = climbPos;

		double targetX = climbPos.getX() + 0.5D;
		double targetZ = climbPos.getZ() + 0.5D;
		double dx = targetX - this.getX();
		double dz = targetZ - this.getZ();
		double horizontalSqr = dx * dx + dz * dz;
		this.getLookControl().setLookAt(player, 30.0F, 30.0F);

		if (horizontalSqr > CLIMB_REACH_DISTANCE_SQR) {
			this.getNavigation().moveTo(targetX, climbPos.getY(), targetZ, 1.1D);
			if (horizontalSqr < 9.0D) {
				Vec3 movement = this.getDeltaMovement();
				this.setDeltaMovement(movement.x * 0.45D + this.clamp(dx * 0.12D, -0.09D, 0.09D), movement.y, movement.z * 0.45D + this.clamp(dz * 0.12D, -0.09D, 0.09D));
			}
			return true;
		}

		this.getNavigation().stop();
		Vec3 movement = this.getDeltaMovement();
		double yBoost = player.getY() > this.getY() + 0.45D ? 0.2D : 0.08D;
		this.setDeltaMovement(this.clamp(dx * 0.18D, -0.08D, 0.08D), Math.max(movement.y, yBoost), this.clamp(dz * 0.18D, -0.08D, 0.08D));
		this.getJumpControl().jump();
		return true;
	}

	private BlockPos findClosestClimbBlockInCommittedColumn(ServerLevel serverLevel) {
		if (this.activeClimbPos == null) {
			return null;
		}
		int currentY = this.blockPosition().getY();
		for (int y = currentY + 2; y >= currentY - 2; y--) {
			BlockPos pos = new BlockPos(this.activeClimbPos.getX(), y, this.activeClimbPos.getZ());
			if (this.isClimbableRouteBlock(serverLevel.getBlockState(pos))) {
				return pos;
			}
		}
		for (int y = currentY + 3; y <= currentY + CLIMB_SCAN_UP; y++) {
			BlockPos pos = new BlockPos(this.activeClimbPos.getX(), y, this.activeClimbPos.getZ());
			if (this.isClimbableRouteBlock(serverLevel.getBlockState(pos))) {
				return pos;
			}
		}
		return this.isClimbableRouteBlock(serverLevel.getBlockState(this.activeClimbPos)) ? this.activeClimbPos : null;
	}

	private boolean tickActiveVerticalRoute(ServerPlayer player, ServerLevel serverLevel) {
		if (this.activeVerticalRoutePos == null) {
			return false;
		}
		if (this.activeVerticalRouteTicks-- <= 0 || player.getY() <= this.getY() + 0.7D) {
			this.clearVerticalMovementCommit();
			return false;
		}
		if (!this.canStandAt(serverLevel, this.activeVerticalRoutePos)) {
			this.clearVerticalMovementCommit();
			return false;
		}

		double targetX = this.activeVerticalRoutePos.getX() + 0.5D;
		double targetY = this.activeVerticalRoutePos.getY();
		double targetZ = this.activeVerticalRoutePos.getZ() + 0.5D;
		this.getLookControl().setLookAt(player, 30.0F, 30.0F);
		if (this.distanceToSqr(targetX, targetY, targetZ) > VERTICAL_ROUTE_REACH_DISTANCE_SQR) {
			this.getNavigation().moveTo(targetX, targetY, targetZ, 1.08D);
			return true;
		}

		this.getNavigation().moveTo(player, 1.08D);
		this.getJumpControl().jump();
		return true;
	}

	private void clearVerticalMovementCommit() {
		this.activeClimbPos = null;
		this.activeClimbTicks = 0;
		this.activeVerticalRoutePos = null;
		this.activeVerticalRouteTicks = 0;
	}

	private double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private boolean tickSmartCombatPlanner(ServerPlayer player, ServerLevel serverLevel) {
		if (this.isWaterPursuit(player)) {
			this.clearBuildColumnPlan();
			return false;
		}
		if (this.breakingBlockPos != null) {
			this.getNavigation().stop();
			return true;
		}
		if (this.pendingBreachTntPos != null) {
			this.getNavigation().stop();
			this.getLookControl().setLookAt(this.pendingBreachTntPos.getX() + 0.5D, this.pendingBreachTntPos.getY() + 0.5D, this.pendingBreachTntPos.getZ() + 0.5D);
			return true;
		}
		if (this.activeDoorPlanPos != null && this.tickActiveDoorPlan(player, serverLevel)) {
			return true;
		}
		if (this.activeBuildColumnPos != null && this.tickActiveBuildColumn(player, serverLevel)) {
			return true;
		}
		if (this.tickCommittedVerticalMovement(player, serverLevel)) {
			return true;
		}

		boolean verticalProblem = player.getY() > this.getY() + 1.35D;
		if (verticalProblem && (this.tryUseNearbyClimbable(player, serverLevel) || this.tryUseNearbyVerticalRoute(player, serverLevel))) {
			return true;
		}
		if (this.isDirectBreachSituation(player) && this.tryDirectChaseBreach(player, serverLevel)) {
			return true;
		}
		boolean pathLooksBad = !this.getNavigation().isInProgress() || this.getNavigation().isDone() || this.stuckTicks >= 3;
		if ((!verticalProblem && !pathLooksBad) || this.smartPlanCooldown > 0) {
			return false;
		}

		this.smartPlanCooldown = SMART_PLAN_COOLDOWN_TICKS;
		if (this.tryOpenNearbyDoor(player, serverLevel)) {
			return true;
		}
		if (this.tryUseNearbyClimbable(player, serverLevel)) {
			return true;
		}
		if (this.tryStartBreakingNearbyClosedDoor(player, serverLevel)) {
			return true;
		}
		if (this.stuckTicks >= STUCK_BREAK_TICKS && this.tryStartBreakingObstacle(player, serverLevel)) {
			return true;
		}
		if (verticalProblem && this.tryUseBuildColumnPlan(player, serverLevel)) {
			return true;
		}
		return false;
	}

	private boolean tickActiveDoorPlan(ServerPlayer player, ServerLevel serverLevel) {
		if (this.activeDoorPlanPos == null) {
			return false;
		}
		if (this.activeDoorPlanTicks-- <= 0) {
			this.rejectDoorPlan();
			this.clearDoorPlan();
			return false;
		}

		BlockState state = serverLevel.getBlockState(this.activeDoorPlanPos);
		if (!(state.getBlock() instanceof DoorBlock doorBlock)) {
			this.clearDoorPlan();
			return false;
		}

		double targetX = this.activeDoorPlanPos.getX() + 0.5D;
		double targetY = this.activeDoorPlanPos.getY();
		double targetZ = this.activeDoorPlanPos.getZ() + 0.5D;
		double currentDistanceSqr = this.distanceToSqr(targetX, targetY, targetZ);
		if (currentDistanceSqr + DOOR_PROGRESS_EPSILON < this.activeDoorPlanLastDistanceSqr) {
			this.activeDoorPlanLastDistanceSqr = currentDistanceSqr;
			this.activeDoorPlanNoProgressTicks = 0;
		} else if (currentDistanceSqr > DOOR_PLAN_REACH_DISTANCE_SQR) {
			this.activeDoorPlanNoProgressTicks++;
			if (this.activeDoorPlanNoProgressTicks >= DOOR_PLAN_NO_PROGRESS_TICKS) {
				this.rejectDoorPlan();
				this.clearDoorPlan();
				this.smartPlanCooldown = 0;
				return false;
			}
		}
		this.getLookControl().setLookAt(targetX, targetY + 0.75D, targetZ);
		if (currentDistanceSqr > DOOR_PLAN_REACH_DISTANCE_SQR) {
			this.getNavigation().moveTo(targetX, targetY, targetZ, 1.08D);
			return true;
		}

		if (doorBlock.isOpen(state)) {
			this.clearDoorPlan();
			this.smartPlanCooldown = 0;
			this.getNavigation().moveTo(player, 1.08D);
			return true;
		}

		this.getNavigation().stop();
		if (DoorBlock.isWoodenDoor(state)) {
			this.swing(InteractionHand.MAIN_HAND, true);
			doorBlock.setOpen(this, serverLevel, state, this.activeDoorPlanPos, true);
			this.clearDoorPlan();
			this.getNavigation().moveTo(player, 1.08D);
			return true;
		}

		if (this.isBreakableObstacle(state, serverLevel, this.activeDoorPlanPos)) {
			BlockPos doorPos = this.activeDoorPlanPos;
			this.clearDoorPlan();
			this.startBreakingBlock(serverLevel, doorPos, state);
			return true;
		}

		this.clearDoorPlan();
		return false;
	}

	private boolean tryUseBroadDoorPlan(ServerPlayer player, ServerLevel serverLevel) {
		if (this.activeBuildColumnPos != null) {
			return false;
		}
		BlockPos door = this.findBroadDoorRoute(player, serverLevel);
		if (door == null) {
			return false;
		}
		this.clearBreakingState(serverLevel);
		this.clearVerticalMovementCommit();
		this.activeDoorPlanPos = door;
		this.activeDoorPlanTicks = DOOR_PLAN_COMMIT_TICKS;
		this.activeDoorPlanLastDistanceSqr = this.distanceToSqr(door.getX() + 0.5D, door.getY(), door.getZ() + 0.5D);
		this.activeDoorPlanNoProgressTicks = 0;
		return this.tickActiveDoorPlan(player, serverLevel);
	}

	private boolean tryUseBroadClimbPlan(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos climbable = this.findBroadClimbableRoute(player, serverLevel);
		if (climbable == null) {
			return false;
		}
		this.clearBreakingState(serverLevel);
		this.clearDoorPlan();
		this.activeClimbPos = climbable;
		this.activeClimbTicks = CLIMB_COMMIT_TICKS;
		this.activeVerticalRoutePos = null;
		this.activeVerticalRouteTicks = 0;
		return this.tickActiveClimb(player, serverLevel);
	}

	private boolean tryUseBroadVerticalRoutePlan(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos route = this.findBroadVerticalRoute(player, serverLevel);
		if (route == null) {
			return false;
		}
		this.clearBreakingState(serverLevel);
		this.clearDoorPlan();
		this.activeVerticalRoutePos = route;
		this.activeVerticalRouteTicks = VERTICAL_ROUTE_COMMIT_TICKS;
		this.activeClimbPos = null;
		this.activeClimbTicks = 0;
		return this.tickActiveVerticalRoute(player, serverLevel);
	}

	private boolean tryUseBuildColumnPlan(ServerPlayer player, ServerLevel serverLevel) {
		if (this.obstacleActionCooldown > 0 || !this.onGround()) {
			return false;
		}
		boolean routeActuallyFailed = this.stuckTicks >= STUCK_BREAK_TICKS * 2 || !this.getNavigation().isInProgress() || this.getNavigation().isDone();
		if (!routeActuallyFailed) {
			return false;
		}
		BlockPos column = this.findBuildColumnCandidate(player, serverLevel);
		if (column == null) {
			return false;
		}
		this.clearBreakingState(serverLevel);
		this.clearDoorPlan();
		this.clearVerticalMovementCommit();
		this.activeBuildColumnPos = column;
		this.activeBuildColumnTicks = BUILD_COLUMN_COMMIT_TICKS;
		this.activeBuildColumnBlocksPlaced = 0;
		this.activeBuildColumnPlaceDelay = BUILD_COLUMN_PLACE_DELAY_TICKS;
		return true;
	}

	private boolean isDirectBreachSituation(ServerPlayer player) {
		return player.getY() - this.getY() >= DIRECT_BREACH_VERTICAL_GAP
				&& this.horizontalDistanceSqr(player) <= DIRECT_BREACH_HORIZONTAL_RANGE * DIRECT_BREACH_HORIZONTAL_RANGE;
	}

	private boolean tryDirectChaseBreach(ServerPlayer player, ServerLevel serverLevel) {
		this.directBreachTicks++;
		if (this.tryOpenNearbyDoor(player, serverLevel)) {
			this.directBreachTicks = 0;
			return true;
		}
		if (this.tryUseNearbyClimbable(player, serverLevel) || this.tryUseNearbyVerticalRoute(player, serverLevel)) {
			this.directBreachTicks = 0;
			return true;
		}

		BlockPos obstacle = this.findReachableBreachObstacle(player, serverLevel);
		if (obstacle != null) {
			if (this.directBreachTicks >= DIRECT_BREACH_TNT_AFTER_TICKS && this.directBreachTntCooldown <= 0 && this.tryUseBreachTnt(serverLevel, obstacle)) {
				this.directBreachTicks = 0;
				return true;
			}
			this.startBreakingBlock(serverLevel, obstacle, serverLevel.getBlockState(obstacle));
			return true;
		}

		if (this.tryUseBuildColumnPlan(player, serverLevel)) {
			return true;
		}

		if (this.directBreachTntCooldown <= 0) {
			BlockPos blastPos = this.findBestBreachBlastPos(player, serverLevel);
			if (blastPos != null && this.tryUseBreachTnt(serverLevel, blastPos)) {
				this.directBreachTicks = 0;
				return true;
			}
		}
		return false;
	}

	private BlockPos findReachableBreachObstacle(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos center = this.blockPosition();
		BlockPos playerPos = player.blockPosition();
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (int y = 0; y <= 4; y++) {
			for (int x = -2; x <= 2; x++) {
				for (int z = -2; z <= 2; z++) {
					if (Math.abs(x) + Math.abs(z) > 3) {
						continue;
					}
					BlockPos pos = center.offset(x, y, z);
					if (!this.isUsefulBreachBlock(serverLevel, pos) || this.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > DIRECT_BREACH_REACH_DISTANCE_SQR) {
						continue;
					}
					double score = this.horizontalDistanceSqr(pos, playerPos) * 0.55D + Math.abs(playerPos.getY() - pos.getY()) * 0.25D + this.horizontalDistanceSqr(pos) * 0.2D + y * 0.1D;
					if (score < bestScore) {
						bestScore = score;
						best = pos;
					}
				}
			}
		}
		return best;
	}

	private BlockPos findBestBreachBlastPos(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos obstacle = this.findReachableBreachObstacle(player, serverLevel);
		if (obstacle != null) {
			return obstacle;
		}
		BlockPos center = this.blockPosition();
		BlockPos playerPos = player.blockPosition();
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (int y = 1; y <= 6; y++) {
			for (int x = -3; x <= 3; x++) {
				for (int z = -3; z <= 3; z++) {
					if (Math.abs(x) + Math.abs(z) > 4) {
						continue;
					}
					BlockPos pos = center.offset(x, y, z);
					if (!this.isUsefulBreachBlock(serverLevel, pos)) {
						continue;
					}
					double score = this.horizontalDistanceSqr(pos, playerPos) + Math.abs(playerPos.getY() - pos.getY()) * 0.35D;
					if (score < bestScore) {
						bestScore = score;
						best = pos;
					}
				}
			}
		}
		return best;
	}

	private boolean isUsefulBreachBlock(ServerLevel serverLevel, BlockPos pos) {
		BlockState state = serverLevel.getBlockState(pos);
		if (state.isAir() || state.liquid() || state.hasBlockEntity() || state.getDestroySpeed(serverLevel, pos) < 0.0F) {
			return false;
		}
		return !state.getCollisionShape(serverLevel, pos).isEmpty();
	}

	private boolean tryUseBreachTnt(ServerLevel serverLevel, BlockPos pos) {
		BlockPos placementPos = this.findTntPlacementNear(serverLevel, pos);
		if (placementPos == null) {
			return false;
		}

		this.clearBreakingState(serverLevel);
		this.clearDoorPlan();
		this.clearVerticalMovementCommit();
		this.clearBuildColumnPlan();
		this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TNT));
		this.swing(InteractionHand.MAIN_HAND, true);
		serverLevel.setBlockAndUpdate(placementPos, Blocks.TNT.defaultBlockState());
		serverLevel.playSound(null, placementPos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.8F, 0.75F);
		this.pendingBreachTntPos = placementPos;
		this.pendingBreachTntTicks = DIRECT_BREACH_TNT_IGNITE_DELAY_TICKS;
		this.directBreachTntCooldown = DIRECT_BREACH_TNT_COOLDOWN_TICKS;
		this.usingUtilityTool = true;
		return true;
	}

	private void tickPendingBreachTnt(ServerLevel serverLevel) {
		if (this.pendingBreachTntPos == null) {
			return;
		}
		if (this.pendingBreachTntTicks-- > 0) {
			return;
		}

		BlockPos tntPos = this.pendingBreachTntPos;
		this.pendingBreachTntPos = null;
		this.pendingBreachTntTicks = 0;
		if (!serverLevel.getBlockState(tntPos).is(Blocks.TNT)) {
			return;
		}

		this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
		this.swing(InteractionHand.MAIN_HAND, true);
		serverLevel.playSound(null, tntPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.9F, 0.9F);
		if (TntBlock.prime(serverLevel, tntPos)) {
			serverLevel.setBlock(tntPos, Blocks.AIR.defaultBlockState(), 11);
		}
	}

	private BlockPos findTntPlacementNear(ServerLevel serverLevel, BlockPos obstaclePos) {
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (Direction direction : Direction.values()) {
			BlockPos candidate = obstaclePos.relative(direction);
			if (!serverLevel.getBlockState(candidate).isAir()) {
				continue;
			}
			BlockState below = serverLevel.getBlockState(candidate.below());
			if (below.getCollisionShape(serverLevel, candidate.below()).isEmpty() && direction != Direction.DOWN) {
				continue;
			}
			double score = this.distanceToSqr(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D);
			if (score < bestScore) {
				bestScore = score;
				best = candidate;
			}
		}
		return best;
	}

	private boolean tickActiveBuildColumn(ServerPlayer player, ServerLevel serverLevel) {
		if (this.activeBuildColumnPos == null) {
			return false;
		}
		if (this.isWaterPursuit(player) || this.activeBuildColumnTicks-- <= 0 || player.getY() <= this.getY() + 1.1D) {
			this.clearBuildColumnPlan();
			return false;
		}

		double targetX = this.activeBuildColumnPos.getX() + 0.5D;
		double targetY = this.activeBuildColumnPos.getY();
		double targetZ = this.activeBuildColumnPos.getZ() + 0.5D;
		this.getLookControl().setLookAt(player, 30.0F, 30.0F);
		if (this.distanceToSqr(targetX, targetY, targetZ) > BUILD_COLUMN_REACH_DISTANCE_SQR && this.getY() <= this.activeBuildColumnPos.getY() + 1.2D) {
			this.getNavigation().moveTo(targetX, targetY, targetZ, 1.08D);
			return true;
		}

		this.getNavigation().stop();
		if (this.activeBuildColumnBlocksPlaced >= BUILD_COLUMN_MAX_BLOCKS) {
			this.clearBuildColumnPlan();
			this.obstacleActionCooldown = BUILD_ACTION_COOLDOWN_TICKS;
			return false;
		}
		if (this.activeBuildColumnPlaceDelay > 0) {
			this.activeBuildColumnPlaceDelay--;
			return true;
		}

		BlockPos feet = BlockPos.containing(this.getX(), this.getY(), this.getZ());
		BlockState feetState = serverLevel.getBlockState(feet);
		BlockState headState = serverLevel.getBlockState(feet.above());
		BlockState nextHeadState = serverLevel.getBlockState(feet.above(2));
		if (feetState.liquid() || serverLevel.getBlockState(feet.below()).liquid() || !feetState.getCollisionShape(serverLevel, feet).isEmpty()) {
			this.clearBuildColumnPlan();
			return false;
		}
		if (!headState.getCollisionShape(serverLevel, feet.above()).isEmpty()) {
			this.startBreakingBlock(serverLevel, feet.above(), headState);
			return true;
		}
		if (!nextHeadState.getCollisionShape(serverLevel, feet.above(2)).isEmpty()) {
			this.startBreakingBlock(serverLevel, feet.above(2), nextHeadState);
			return true;
		}

		this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COBBLESTONE));
		this.usingUtilityTool = true;
		this.swing(InteractionHand.MAIN_HAND, true);
		serverLevel.setBlockAndUpdate(feet, Blocks.COBBLESTONE.defaultBlockState());
		this.activeBuildColumnBlocksPlaced++;
		this.activeBuildColumnPlaceDelay = BUILD_COLUMN_PLACE_DELAY_TICKS;
		this.teleportTo(this.getX(), this.getY() + 1.0D, this.getZ());
		serverLevel.playSound(null, this.blockPosition(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.75F, 0.85F);
		return true;
	}

	private BlockPos findBuildColumnCandidate(ServerPlayer player, ServerLevel serverLevel) {
		if (player.getY() - this.getY() < BUILD_MIN_VERTICAL_GAP) {
			return null;
		}
		BlockPos center = this.blockPosition();
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (int x = -BUILD_COLUMN_SCAN_RADIUS; x <= BUILD_COLUMN_SCAN_RADIUS; x++) {
			for (int z = -BUILD_COLUMN_SCAN_RADIUS; z <= BUILD_COLUMN_SCAN_RADIUS; z++) {
				if (Math.abs(x) + Math.abs(z) > BUILD_COLUMN_SCAN_RADIUS + 3) {
					continue;
				}
				for (int y = -2; y <= 2; y++) {
					BlockPos candidate = center.offset(x, y, z);
					if (!this.canStandAt(serverLevel, candidate)) {
						continue;
					}
					double toPlayerHorizontal = this.horizontalDistanceSqr(candidate, player.blockPosition());
					if (toPlayerHorizontal > 144.0D) {
						continue;
					}
					double score = this.horizontalDistanceSqr(candidate) + toPlayerHorizontal * 0.35D + Math.abs(candidate.getY() - this.getY()) * 0.5D;
					if (score < bestScore) {
						bestScore = score;
						best = candidate;
					}
				}
			}
		}
		return best;
	}

	private boolean hasClearAscentColumn(ServerLevel serverLevel, BlockPos standPos, int targetY) {
		int startY = standPos.getY();
		int endY = Math.max(startY + 2, Math.min(targetY, startY + 24));
		for (int y = startY; y <= endY; y++) {
			BlockPos feet = new BlockPos(standPos.getX(), y, standPos.getZ());
			BlockPos head = feet.above();
			if (!serverLevel.getBlockState(feet).getCollisionShape(serverLevel, feet).isEmpty() || !serverLevel.getBlockState(head).getCollisionShape(serverLevel, head).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private BlockPos findBroadDoorRoute(ServerPlayer player, ServerLevel serverLevel) {
		return this.findBestDoorInArea(serverLevel, this.blockPosition(), player.blockPosition(), true);
	}

	private BlockPos findBestDoorInArea(ServerLevel serverLevel, BlockPos firstCenter, BlockPos secondCenter, boolean includePlayerCenter) {
		BlockPos best = this.findBestDoorAround(serverLevel, firstCenter, secondCenter);
		if (includePlayerCenter) {
			BlockPos playerDoor = this.findBestDoorAround(serverLevel, secondCenter, firstCenter);
			if (playerDoor != null && (best == null || this.scoreDoor(playerDoor, secondCenter) < this.scoreDoor(best, secondCenter))) {
				best = playerDoor;
			}
		}
		return best;
	}

	private BlockPos findBestDoorAround(ServerLevel serverLevel, BlockPos center, BlockPos otherImportantPos) {
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (int x = -SMART_PLAN_SCAN_RADIUS; x <= SMART_PLAN_SCAN_RADIUS; x++) {
			for (int z = -SMART_PLAN_SCAN_RADIUS; z <= SMART_PLAN_SCAN_RADIUS; z++) {
				if (Math.abs(x) + Math.abs(z) > SMART_PLAN_SCAN_RADIUS + 8) {
					continue;
				}
				for (int y = -SMART_PLAN_SCAN_DOWN; y <= SMART_PLAN_SCAN_UP; y++) {
					BlockPos pos = center.offset(x, y, z);
					BlockState state = serverLevel.getBlockState(pos);
					if (!(state.getBlock() instanceof DoorBlock)) {
						continue;
					}
					if (this.isRejectedDoorPlan(pos)) {
						continue;
					}
					double score = this.horizontalDistanceSqr(pos) + this.horizontalDistanceSqr(pos, otherImportantPos) * 0.45D + Math.abs(pos.getY() - this.getY()) * 0.65D;
					if (score < bestScore) {
						bestScore = score;
						best = pos;
					}
				}
			}
		}
		return best;
	}

	private double scoreDoor(BlockPos pos, BlockPos otherImportantPos) {
		return this.horizontalDistanceSqr(pos) + this.horizontalDistanceSqr(pos, otherImportantPos) * 0.45D + Math.abs(pos.getY() - this.getY()) * 0.65D;
	}

	private BlockPos findBroadClimbableRoute(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos best = this.findBroadClimbableAround(player, serverLevel, this.blockPosition());
		BlockPos playerSide = this.findBroadClimbableAround(player, serverLevel, player.blockPosition());
		if (playerSide != null && (best == null || this.scoreClimbCandidate(playerSide, player) < this.scoreClimbCandidate(best, player))) {
			best = playerSide;
		}
		return best;
	}

	private BlockPos findBroadClimbableAround(ServerPlayer player, ServerLevel serverLevel, BlockPos center) {
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (int x = -SMART_PLAN_SCAN_RADIUS; x <= SMART_PLAN_SCAN_RADIUS; x++) {
			for (int z = -SMART_PLAN_SCAN_RADIUS; z <= SMART_PLAN_SCAN_RADIUS; z++) {
				if (Math.abs(x) + Math.abs(z) > SMART_PLAN_SCAN_RADIUS + 8) {
					continue;
				}
				for (int y = -SMART_PLAN_SCAN_DOWN; y <= SMART_PLAN_SCAN_UP; y++) {
					BlockPos pos = center.offset(x, y, z);
					if (!this.isClimbableRouteBlock(serverLevel.getBlockState(pos)) || !this.climbableColumnCanMatter(serverLevel, pos, player)) {
						continue;
					}
					BlockPos entry = this.findClimbColumnEntry(serverLevel, pos);
					double score = this.scoreClimbCandidate(entry, player);
					if (score < bestScore) {
						bestScore = score;
						best = entry;
					}
				}
			}
		}
		return best;
	}

	private boolean climbableColumnCanMatter(ServerLevel serverLevel, BlockPos pos, ServerPlayer player) {
		int topY = pos.getY();
		for (int i = 1; i <= SMART_PLAN_SCAN_UP; i++) {
			BlockPos above = new BlockPos(pos.getX(), pos.getY() + i, pos.getZ());
			if (!this.isClimbableRouteBlock(serverLevel.getBlockState(above))) {
				break;
			}
			topY = above.getY();
		}
		return topY >= player.getY() - 2.0D || (topY > this.getY() + 2.0D && this.horizontalDistanceSqr(pos, player.blockPosition()) < 144.0D);
	}

	private BlockPos findClimbColumnEntry(ServerLevel serverLevel, BlockPos pos) {
		int y = pos.getY();
		int minY = Math.max(serverLevel.getMinY(), (int) Math.floor(this.getY()) - SMART_PLAN_SCAN_DOWN);
		while (y > minY) {
			BlockPos below = new BlockPos(pos.getX(), y - 1, pos.getZ());
			if (!this.isClimbableRouteBlock(serverLevel.getBlockState(below))) {
				break;
			}
			y--;
		}
		return new BlockPos(pos.getX(), y, pos.getZ());
	}

	private double scoreClimbCandidate(BlockPos pos, ServerPlayer player) {
		return this.horizontalDistanceSqr(pos) + this.horizontalDistanceSqr(pos, player.blockPosition()) * 0.25D + Math.abs(pos.getY() - this.getY()) * 0.5D;
	}

	private BlockPos findBroadVerticalRoute(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos best = this.findBroadVerticalRouteAround(player, serverLevel, this.blockPosition());
		BlockPos playerSide = this.findBroadVerticalRouteAround(player, serverLevel, player.blockPosition());
		if (playerSide != null && (best == null || this.scoreVerticalRoute(playerSide, player) < this.scoreVerticalRoute(best, player))) {
			best = playerSide;
		}
		return best;
	}

	private BlockPos findBroadVerticalRouteAround(ServerPlayer player, ServerLevel serverLevel, BlockPos center) {
		if (player.getY() <= this.getY() + 1.35D) {
			return null;
		}
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (int x = -SMART_PLAN_SCAN_RADIUS; x <= SMART_PLAN_SCAN_RADIUS; x++) {
			for (int z = -SMART_PLAN_SCAN_RADIUS; z <= SMART_PLAN_SCAN_RADIUS; z++) {
				if (Math.abs(x) + Math.abs(z) > SMART_PLAN_SCAN_RADIUS + 8) {
					continue;
				}
				for (int y = -1; y <= SMART_PLAN_SCAN_UP; y++) {
					BlockPos floorPos = center.offset(x, y, z);
					BlockState floorState = serverLevel.getBlockState(floorPos);
					BlockPos standPos = floorPos.above();
					if (!this.isLikelyVerticalRouteBlock(floorState) || !this.canStandAt(serverLevel, standPos)) {
						continue;
					}
					double score = this.scoreVerticalRoute(standPos, player);
					if (score < bestScore) {
						bestScore = score;
						best = standPos;
					}
				}
			}
		}
		return best;
	}

	private double scoreVerticalRoute(BlockPos standPos, ServerPlayer player) {
		return this.horizontalDistanceSqr(standPos) + this.horizontalDistanceSqr(standPos, player.blockPosition()) * 0.35D + Math.abs(standPos.getY() - this.getY()) * 0.45D;
	}

	private void clearDoorPlan() {
		this.activeDoorPlanPos = null;
		this.activeDoorPlanTicks = 0;
		this.activeDoorPlanLastDistanceSqr = 0.0D;
		this.activeDoorPlanNoProgressTicks = 0;
	}

	private void rejectDoorPlan() {
		if (this.activeDoorPlanPos != null) {
			this.rejectedDoorPlanPos = this.activeDoorPlanPos;
			this.rejectedDoorPlanCooldown = REJECTED_DOOR_COOLDOWN_TICKS;
		}
	}

	private boolean isRejectedDoorPlan(BlockPos pos) {
		return this.rejectedDoorPlanCooldown > 0 && this.rejectedDoorPlanPos != null && this.rejectedDoorPlanPos.equals(pos);
	}

	private void clearBuildColumnPlan() {
		this.activeBuildColumnPos = null;
		this.activeBuildColumnTicks = 0;
		this.activeBuildColumnBlocksPlaced = 0;
		this.activeBuildColumnPlaceDelay = 0;
	}

	public String getAiDebugInfo(ServerPlayer player) {
		String plan = "none";
		if (this.breakingBlockPos != null) {
			plan = "breaking " + this.shortPos(this.breakingBlockPos) + " ticks=" + this.breakingTicks + "/" + this.breakingTotalTicks;
		} else if (this.activeDoorPlanPos != null) {
			plan = "door " + this.shortPos(this.activeDoorPlanPos) + " ticks=" + this.activeDoorPlanTicks;
		} else if (this.activeClimbPos != null) {
			plan = "climb " + this.shortPos(this.activeClimbPos) + " ticks=" + this.activeClimbTicks;
		} else if (this.activeVerticalRoutePos != null) {
			plan = "vertical-route " + this.shortPos(this.activeVerticalRoutePos) + " ticks=" + this.activeVerticalRouteTicks;
		} else if (this.activeBuildColumnPos != null) {
			plan = "build-column " + this.shortPos(this.activeBuildColumnPos) + " ticks=" + this.activeBuildColumnTicks;
		} else if (this.pendingBreachTntPos != null) {
			plan = "arming-tnt " + this.shortPos(this.pendingBreachTntPos) + " ticks=" + this.pendingBreachTntTicks;
		} else if (this.directBreachTicks > 0) {
			plan = "direct-breach ticks=" + this.directBreachTicks + " tntCooldown=" + this.directBreachTntCooldown;
		} else if (this.rejectedDoorPlanPos != null && this.rejectedDoorPlanCooldown > 0) {
			plan = "rejected-door " + this.shortPos(this.rejectedDoorPlanPos) + " cooldown=" + this.rejectedDoorPlanCooldown;
		}

		double horizontal = Math.sqrt(this.horizontalDistanceSqr(player));
		double vertical = player.getY() - this.getY();
		return String.format(Locale.ROOT, "AI plan=%s, target=%s, nav=%s/%s, stuck=%d, cooldown=%d, smartCooldown=%d, horizontal=%.1f, vertical=%.1f",
				plan,
				this.getTarget() == null ? "none" : String.valueOf(this.getTarget().getType()),
				this.getNavigation().isInProgress() ? "moving" : "idle",
				this.getNavigation().isDone() ? "done" : "notDone",
				this.stuckTicks,
				this.obstacleActionCooldown,
				this.smartPlanCooldown,
				horizontal,
				vertical);
	}

	private String shortPos(BlockPos pos) {
		return pos.getX() + "," + pos.getY() + "," + pos.getZ();
	}

	private void tickObstacleFreedom(ServerPlayer player, ServerLevel serverLevel) {
		if (this.isWaterPursuit(player)) {
			this.clearBuildColumnPlan();
			this.clearVerticalMovementCommit();
			this.stuckTicks = 0;
			return;
		}
		if (this.breakingBlockPos != null) {
			this.tickBreakingBlock(player, serverLevel);
			return;
		}
		if (this.tickSmartCombatPlanner(player, serverLevel)) {
			this.resetStuckTracker();
			return;
		}
		if (this.tickCommittedVerticalMovement(player, serverLevel)) {
			this.resetStuckTracker();
			return;
		}
		if (player.getY() > this.getY() + 1.35D) {
			if (this.tryUseNearbyClimbable(player, serverLevel) || this.tryUseNearbyVerticalRoute(player, serverLevel)) {
				this.resetStuckTracker();
				return;
			}
		}

		double movedX = this.getX() - this.lastCombatX;
		double movedZ = this.getZ() - this.lastCombatZ;
		this.lastCombatX = this.getX();
		this.lastCombatZ = this.getZ();

		boolean noPath = !this.getNavigation().isInProgress() || this.getNavigation().isDone();
		boolean barelyMoved = movedX * movedX + movedZ * movedZ < 0.0025D;
		if (noPath || barelyMoved) {
			this.stuckTicks++;
		} else {
			this.stuckTicks = Math.max(0, this.stuckTicks - 2);
			return;
		}

		if (this.stuckTicks < STUCK_DOOR_TICKS || this.obstacleActionCooldown > 0) {
			return;
		}

		if (this.tryOpenNearbyDoor(player, serverLevel)) {
			this.obstacleActionCooldown = OBSTACLE_ACTION_COOLDOWN_TICKS;
			this.stuckTicks = 0;
			return;
		}

		if (this.tryUseNearbyClimbable(player, serverLevel)) {
			this.obstacleActionCooldown = CLIMB_ACTION_COOLDOWN_TICKS;
			this.stuckTicks = 0;
			return;
		}

		if (this.tryUseNearbyVerticalRoute(player, serverLevel)) {
			this.obstacleActionCooldown = CLIMB_ACTION_COOLDOWN_TICKS;
			this.stuckTicks = 0;
			return;
		}

		if (this.stuckTicks >= STUCK_BREAK_TICKS && this.tryStartBreakingNearbyClosedDoor(player, serverLevel)) {
			this.stuckTicks = Math.max(0, this.stuckTicks - 8);
			return;
		}

		if (this.tryBuildUpToPlayer(player, serverLevel)) {
			this.obstacleActionCooldown = BUILD_ACTION_COOLDOWN_TICKS;
			this.stuckTicks = 0;
			return;
		}

		if (this.stuckTicks >= STUCK_BREAK_TICKS && this.tryStartBreakingObstacle(player, serverLevel)) {
			this.stuckTicks = Math.max(0, this.stuckTicks - 8);
			return;
		}

		if (this.stuckTicks >= STUCK_TNT_TICKS && this.directBreachTntCooldown <= 0) {
			BlockPos breachTarget = this.findStuckBreachTarget(player, serverLevel);
			if (breachTarget != null && this.tryUseBreachTnt(serverLevel, breachTarget)) {
				this.obstacleActionCooldown = OBSTACLE_ACTION_COOLDOWN_TICKS;
				this.stuckTicks = 0;
				return;
			}
		}

		if (this.stuckTicks >= STUCK_TELEPORT_TICKS && this.tryTacticalTeleport(player, serverLevel)) {
			this.obstacleActionCooldown = TELEPORT_ACTION_COOLDOWN_TICKS;
			this.stuckTicks = 0;
		}
	}

	private void resetStuckTracker() {
		this.stuckTicks = 0;
		this.lastCombatX = this.getX();
		this.lastCombatZ = this.getZ();
	}

	private boolean tryOpenNearbyDoor(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos center = this.blockPosition();
		for (int y = -1; y <= 2; y++) {
			for (int x = -4; x <= 4; x++) {
				for (int z = -4; z <= 4; z++) {
					if (Math.abs(x) + Math.abs(z) > 5) {
						continue;
					}
					BlockPos pos = center.offset(x, y, z);
					BlockState state = serverLevel.getBlockState(pos);
					if (state.getBlock() instanceof DoorBlock doorBlock && DoorBlock.isWoodenDoor(state) && !doorBlock.isOpen(state)) {
						this.swing(InteractionHand.MAIN_HAND, true);
						doorBlock.setOpen(this, serverLevel, state, pos, true);
						this.getNavigation().moveTo(player, 1.08D);
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean tryUseNearbyClimbable(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos climbable = this.findNearbyClimbableRoute(player, serverLevel);
		if (climbable == null) {
			return false;
		}

		this.clearBreakingState(serverLevel);
		this.activeClimbPos = climbable;
		this.activeClimbTicks = CLIMB_COMMIT_TICKS;
		this.activeVerticalRoutePos = null;
		this.activeVerticalRouteTicks = 0;
		return this.tickActiveClimb(player, serverLevel);
	}

	private boolean tryUseNearbyVerticalRoute(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos route = this.findNearbyVerticalRoute(player, serverLevel);
		if (route == null) {
			return false;
		}

		this.clearBreakingState(serverLevel);
		this.activeVerticalRoutePos = route;
		this.activeVerticalRouteTicks = VERTICAL_ROUTE_COMMIT_TICKS;
		this.activeClimbPos = null;
		this.activeClimbTicks = 0;
		return this.tickActiveVerticalRoute(player, serverLevel);
	}

	private boolean tryStartBreakingNearbyClosedDoor(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos center = this.blockPosition();
		for (int y = -1; y <= 2; y++) {
			for (int x = -4; x <= 4; x++) {
				for (int z = -4; z <= 4; z++) {
					if (Math.abs(x) + Math.abs(z) > 5) {
						continue;
					}
					BlockPos pos = center.offset(x, y, z);
					BlockState state = serverLevel.getBlockState(pos);
					if (state.getBlock() instanceof DoorBlock doorBlock && !doorBlock.isOpen(state) && this.isBreakableObstacle(state, serverLevel, pos)) {
						this.startBreakingBlock(serverLevel, pos, state);
						return true;
					}
				}
			}
		}
		return false;
	}

	private BlockPos findNearbyVerticalRoute(ServerPlayer player, ServerLevel serverLevel) {
		if (player.getY() <= this.getY() + 1.35D) {
			return null;
		}
		BlockPos center = this.blockPosition();
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (int x = -VERTICAL_ROUTE_SCAN_RADIUS; x <= VERTICAL_ROUTE_SCAN_RADIUS; x++) {
			for (int z = -VERTICAL_ROUTE_SCAN_RADIUS; z <= VERTICAL_ROUTE_SCAN_RADIUS; z++) {
				if (Math.abs(x) + Math.abs(z) > VERTICAL_ROUTE_SCAN_RADIUS + 2) {
					continue;
				}
				for (int y = 1; y <= VERTICAL_ROUTE_SCAN_UP; y++) {
					BlockPos floorPos = center.offset(x, y, z);
					BlockState floorState = serverLevel.getBlockState(floorPos);
					BlockPos standPos = floorPos.above();
					if (!this.isLikelyVerticalRouteBlock(floorState) || !this.canStandAt(serverLevel, standPos) || !this.hasLowerStepNear(serverLevel, standPos)) {
						continue;
					}
					double score = this.horizontalDistanceSqr(standPos) + this.horizontalDistanceSqr(standPos, player.blockPosition()) * 0.25D - y * 0.35D;
					if (score < bestScore) {
						bestScore = score;
						best = standPos;
					}
				}
			}
		}
		return best;
	}

	private boolean isLikelyVerticalRouteBlock(BlockState state) {
		String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
		return path.contains("stair") || path.contains("slab") || path.contains("scaffolding");
	}

	private boolean hasLowerStepNear(ServerLevel serverLevel, BlockPos standPos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos lowerStand = standPos.relative(direction).below();
			if (this.canStandAt(serverLevel, lowerStand)) {
				return true;
			}
			BlockState lowerFloor = serverLevel.getBlockState(lowerStand.below());
			if (this.isLikelyVerticalRouteBlock(lowerFloor) && this.canStandAt(serverLevel, lowerStand)) {
				return true;
			}
		}
		return false;
	}

	private BlockPos findNearbyClimbableRoute(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos center = this.blockPosition();
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (int x = -CLIMB_SCAN_RADIUS; x <= CLIMB_SCAN_RADIUS; x++) {
			for (int z = -CLIMB_SCAN_RADIUS; z <= CLIMB_SCAN_RADIUS; z++) {
				if (Math.abs(x) + Math.abs(z) > CLIMB_SCAN_RADIUS + 2) {
					continue;
				}
				for (int y = -1; y <= CLIMB_SCAN_UP; y++) {
					BlockPos pos = center.offset(x, y, z);
					BlockState state = serverLevel.getBlockState(pos);
					if (!this.isClimbableRouteBlock(state) || !this.climbableRouteHelpsReachPlayer(pos, player)) {
						continue;
					}
					double score = this.horizontalDistanceSqr(pos) + this.horizontalDistanceSqr(pos, player.blockPosition()) * 0.35D - Math.max(0, pos.getY() - center.getY()) * 0.45D;
					if (score < bestScore) {
						bestScore = score;
						best = pos;
					}
				}
			}
		}
		return best;
	}

	private boolean climbableRouteHelpsReachPlayer(BlockPos pos, ServerPlayer player) {
		return player.getY() > this.getY() + 0.75D || Math.abs(player.getY() - pos.getY()) <= 3.0D;
	}

	private boolean isClimbableRouteBlock(BlockState state) {
		String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
		return path.contains("ladder") || path.contains("vine") || path.contains("scaffolding");
	}

	private double horizontalDistanceSqr(BlockPos pos) {
		double x = this.getX() - (pos.getX() + 0.5D);
		double z = this.getZ() - (pos.getZ() + 0.5D);
		return x * x + z * z;
	}

	private double horizontalDistanceSqr(BlockPos a, BlockPos b) {
		double x = a.getX() - b.getX();
		double z = a.getZ() - b.getZ();
		return x * x + z * z;
	}

	private boolean tryStartBreakingObstacle(ServerPlayer player, ServerLevel serverLevel) {
		for (BlockPos pos : this.getObstacleProbePositions(player)) {
			BlockState state = serverLevel.getBlockState(pos);
			if (this.isBreakableObstacle(state, serverLevel, pos)) {
				this.startBreakingBlock(serverLevel, pos, state);
				return true;
			}
		}
		return false;
	}

	private BlockPos findStuckBreachTarget(ServerPlayer player, ServerLevel serverLevel) {
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		for (BlockPos pos : this.getObstacleProbePositions(player)) {
			if (!this.isUsefulBreachBlock(serverLevel, pos)) {
				continue;
			}
			double score = this.horizontalDistanceSqr(pos, player.blockPosition()) * 0.7D + this.horizontalDistanceSqr(pos) * 0.3D;
			if (score < bestScore) {
				bestScore = score;
				best = pos;
			}
		}
		return best;
	}

	private void startBreakingBlock(ServerLevel serverLevel, BlockPos pos, BlockState state) {
		if (this.breakingBlockPos != null && this.breakingBlockPos.equals(pos)) {
			return;
		}
		this.clearBreakingState(serverLevel);
		this.clearVerticalMovementCommit();
		this.clearDoorPlan();
		this.clearBuildColumnPlan();
		this.breakingBlockPos = pos;
		this.breakingTicks = 0;
		this.breakingTotalTicks = this.getSurvivalBreakTicks(state, serverLevel, pos);
		this.setItemInHand(InteractionHand.MAIN_HAND, this.getToolForBlock(state));
		this.usingUtilityTool = true;
		this.getNavigation().stop();
		this.swing(InteractionHand.MAIN_HAND, true);
		serverLevel.destroyBlockProgress(this.getId(), pos, 0);
	}

	private void tickBreakingBlock(ServerPlayer player, ServerLevel serverLevel) {
		if (this.breakingBlockPos == null) {
			return;
		}

		BlockState state = serverLevel.getBlockState(this.breakingBlockPos);
		if (!this.isBreakableObstacle(state, serverLevel, this.breakingBlockPos)) {
			this.clearBreakingState(serverLevel);
			return;
		}

		this.getNavigation().stop();
		this.getLookControl().setLookAt(this.breakingBlockPos.getX() + 0.5D, this.breakingBlockPos.getY() + 0.5D, this.breakingBlockPos.getZ() + 0.5D);
		if (this.breakingTicks % 8 == 0) {
			this.swing(InteractionHand.MAIN_HAND, true);
		}

		this.breakingTicks++;
		int progress = Math.min(9, (int) ((this.breakingTicks * 10.0F) / Math.max(1, this.breakingTotalTicks)));
		serverLevel.destroyBlockProgress(this.getId(), this.breakingBlockPos, progress);

		if (this.breakingTicks >= this.breakingTotalTicks) {
			BlockPos brokenPos = this.breakingBlockPos;
			serverLevel.levelEvent(null, 2001, brokenPos, Block.getId(state));
			serverLevel.destroyBlock(brokenPos, false, this, 512);
			this.clearBreakingState(serverLevel);
			this.getNavigation().moveTo(player, 1.08D);
		}
	}

	private void clearBreakingState(ServerLevel serverLevel) {
		if (serverLevel != null && this.breakingBlockPos != null) {
			serverLevel.destroyBlockProgress(this.getId(), this.breakingBlockPos, -1);
		}
		this.breakingBlockPos = null;
		this.breakingTicks = 0;
		this.breakingTotalTicks = 0;
	}

	private BlockPos[] getObstacleProbePositions(LivingEntity target) {
		Vec3 direction = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
		if (direction.lengthSqr() < 0.0001D) {
			direction = this.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
		}
		if (direction.lengthSqr() < 0.0001D) {
			direction = new Vec3(0.0D, 0.0D, 1.0D);
		}
		direction = direction.normalize();

		BlockPos foot = BlockPos.containing(this.getX(), this.getY(), this.getZ());
		BlockPos ahead = BlockPos.containing(this.getX() + direction.x, this.getY(), this.getZ() + direction.z);
		BlockPos aheadTwo = BlockPos.containing(this.getX() + direction.x * 2.0D, this.getY(), this.getZ() + direction.z * 2.0D);
		BlockPos aheadThree = BlockPos.containing(this.getX() + direction.x * 3.0D, this.getY(), this.getZ() + direction.z * 3.0D);

		return new BlockPos[]{
				ahead,
				ahead.above(),
				aheadTwo,
				aheadTwo.above(),
				aheadThree,
				aheadThree.above(),
				foot,
				foot.above()
		};
	}

	private boolean isBreakableObstacle(BlockState state, ServerLevel serverLevel, BlockPos pos) {
		if (state.isAir() || state.liquid() || state.hasBlockEntity()) {
			return false;
		}
		if (state.getDestroySpeed(serverLevel, pos) < 0.0F) {
			return false;
		}
		return true;
	}

	private ItemStack getToolForBlock(BlockState state) {
		if (state.typeHolder().is(BlockTags.SWORD_INSTANTLY_MINES) || state.typeHolder().is(BlockTags.SWORD_EFFICIENT)) {
			return new ItemStack(Items.DIAMOND_SWORD);
		}
		if (state.typeHolder().is(BlockTags.MINEABLE_WITH_PICKAXE) || this.isGlassLike(state)) {
			return new ItemStack(Items.DIAMOND_PICKAXE);
		}
		if (state.typeHolder().is(BlockTags.MINEABLE_WITH_HOE) || state.typeHolder().is(BlockTags.LEAVES)) {
			return new ItemStack(Items.DIAMOND_HOE);
		}
		if (state.typeHolder().is(BlockTags.WOOL) || state.typeHolder().is(BlockTags.WOOL_CARPETS)) {
			return new ItemStack(Items.SHEARS);
		}
		if (state.typeHolder().is(BlockTags.MINEABLE_WITH_AXE) || state.typeHolder().is(BlockTags.LOGS) || state.typeHolder().is(BlockTags.PLANKS)) {
			return new ItemStack(Items.DIAMOND_AXE);
		}
		if (state.typeHolder().is(BlockTags.MINEABLE_WITH_SHOVEL) || state.typeHolder().is(BlockTags.DIRT) || state.typeHolder().is(BlockTags.SAND) || state.typeHolder().is(BlockTags.SNOW)) {
			return new ItemStack(Items.DIAMOND_SHOVEL);
		}
		return ItemStack.EMPTY;
	}

	private int getSurvivalBreakTicks(BlockState state, ServerLevel serverLevel, BlockPos pos) {
		float hardness = state.getDestroySpeed(serverLevel, pos);
		if (hardness <= 0.0F) {
			return 8;
		}

		double speed = this.getToolSpeed(this.getToolForBlock(state));
		int ticks = (int) Math.ceil(hardness * 30.0D / speed);
		return Math.max(8, Math.min(ticks, 240));
	}

	private double getToolSpeed(ItemStack tool) {
		if (tool.isEmpty()) {
			return 1.0D;
		}
		if (tool.is(Items.DIAMOND_AXE) || tool.is(Items.DIAMOND_PICKAXE) || tool.is(Items.DIAMOND_SHOVEL) || tool.is(Items.DIAMOND_HOE)) {
			return 8.0D;
		}
		if (tool.is(Items.DIAMOND_SWORD)) {
			return 12.0D;
		}
		if (tool.is(Items.SHEARS)) {
			return 5.0D;
		}
		return 1.0D;
	}

	private boolean isGlassLike(BlockState state) {
		return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().contains("glass");
	}

	private boolean tryBuildUpToPlayer(ServerPlayer player, ServerLevel serverLevel) {
		return false;
	}

	private double horizontalDistanceSqr(Entity entity) {
		double x = this.getX() - entity.getX();
		double z = this.getZ() - entity.getZ();
		return x * x + z * z;
	}

	private boolean tryTacticalTeleport(ServerPlayer player, ServerLevel serverLevel) {
		for (int attempt = 0; attempt < 12; attempt++) {
			double angle = this.random.nextDouble() * Math.PI * 2.0D;
			double radius = 3.5D + this.random.nextDouble() * 3.0D;
			double x = player.getX() + Math.cos(angle) * radius;
			double z = player.getZ() + Math.sin(angle) * radius;
			BlockPos origin = BlockPos.containing(x, player.getY(), z);

			for (int yOffset = 2; yOffset >= -3; yOffset--) {
				BlockPos candidate = origin.offset(0, yOffset, 0);
				if (this.canSafeCombatTeleportAt(serverLevel, candidate)) {
					serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 24, 0.35D, 0.6D, 0.35D, 0.03D);
					this.teleportTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D);
					serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 24, 0.35D, 0.6D, 0.35D, 0.03D);
					serverLevel.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8F, 0.75F);
					this.getNavigation().moveTo(player, 1.08D);
					return true;
				}
			}
		}
		return false;
	}

	private void retreatAfterKilling(ServerPlayer player, ServerLevel serverLevel) {
		this.setTarget(null);
		this.getNavigation().stop();
		this.resetCombat();
		this.postKillCooldownTicks = POST_KILL_COOLDOWN_TICKS;
		this.tryPostKillRetreat(player, serverLevel);
	}

	private boolean tryPostKillRetreat(ServerPlayer player, ServerLevel serverLevel) {
		serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 45, 0.35D, 0.75D, 0.35D, 0.04D);

		for (int attempt = 0; attempt < 24; attempt++) {
			double angle = this.random.nextDouble() * Math.PI * 2.0D;
			double radius = POST_KILL_RETREAT_MIN_DISTANCE + this.random.nextDouble() * POST_KILL_RETREAT_EXTRA_DISTANCE;
			double x = player.getX() + Math.cos(angle) * radius;
			double z = player.getZ() + Math.sin(angle) * radius;
			BlockPos origin = BlockPos.containing(x, player.getY(), z);

			for (int yOffset = 8; yOffset >= -8; yOffset--) {
				BlockPos candidate = origin.offset(0, yOffset, 0);
				if (this.canStandAt(serverLevel, candidate)) {
					this.teleportTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D);
					serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 45, 0.35D, 0.75D, 0.35D, 0.04D);
					serverLevel.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.9F, 0.65F);
					return true;
				}
			}
		}

		serverLevel.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.9F, 0.65F);
		return false;
	}

	private boolean canStandAt(ServerLevel serverLevel, BlockPos pos) {
		BlockState feet = serverLevel.getBlockState(pos);
		BlockState head = serverLevel.getBlockState(pos.above());
		BlockState floor = serverLevel.getBlockState(pos.below());
		return feet.getCollisionShape(serverLevel, pos).isEmpty()
				&& head.getCollisionShape(serverLevel, pos.above()).isEmpty()
				&& !floor.getCollisionShape(serverLevel, pos.below()).isEmpty();
	}

	private boolean canSafeCombatTeleportAt(ServerLevel serverLevel, BlockPos pos) {
		if (!this.canStandAt(serverLevel, pos)) {
			return false;
		}
		BlockState feet = serverLevel.getBlockState(pos);
		BlockState head = serverLevel.getBlockState(pos.above());
		BlockState floor = serverLevel.getBlockState(pos.below());
		return !feet.liquid()
				&& !head.liquid()
				&& !floor.liquid()
				&& !feet.is(Blocks.FIRE)
				&& !feet.is(Blocks.SOUL_FIRE)
				&& !floor.is(Blocks.FIRE)
				&& !floor.is(Blocks.SOUL_FIRE);
	}
}
