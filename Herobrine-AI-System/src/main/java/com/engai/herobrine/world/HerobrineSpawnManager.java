package com.engai.herobrine.world;

import com.engai.herobrine.entity.HerobrineDisguiseCowEntity;
import com.engai.herobrine.entity.HerobrineDisguiseSheepEntity;
import com.engai.herobrine.entity.HerobrineEntity;
import com.engai.herobrine.registry.ModEntities;
import com.engai.herobrine.registry.ModSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class HerobrineSpawnManager {
	private static final int DAY_TICKS = 24000;
	private static final int STALK_DAY_ONE_TICKS = 20 * 180;
	private static final int STALK_DAY_TWO_TICKS = 20 * 180;
	private static final int STALK_DAY_THREE_TICKS = 20 * 180;
	private static final int MIN_COMBAT_DAY = 4;
	private static final int MAX_COMBAT_DAY = 5;
	private static final int POST_COMBAT_WAIT_DAYS = 2;
	private static final int FIRST_ATTEMPT_COOLDOWN_TICKS = 20 * 25;
	private static final int POST_DEATH_RESET_COOLDOWN_TICKS = 20 * 180;
	private static final int FAILED_ATTEMPT_COOLDOWN_TICKS = 20 * 25;
	private static final int DAY_ONE_SPAWN_COOLDOWN_TICKS = 20 * 90;
	private static final int DAY_TWO_SPAWN_COOLDOWN_TICKS = 20 * 75;
	private static final int DAY_THREE_SPAWN_COOLDOWN_TICKS = 20 * 70;
	private static final int COMBAT_SPAWN_COOLDOWN_TICKS = 20 * 90;
	private static final int AMBIENCE_MIN_COOLDOWN_TICKS = 20 * 12;
	private static final int AMBIENCE_RANDOM_COOLDOWN_TICKS = 20 * 18;
	private static final double AMBIENCE_APPROACH_DISTANCE = 11.0D;
	private static final int SIGN_FIRST_COOLDOWN_TICKS = 20 * 180;
	private static final int SIGN_MIN_COOLDOWN_TICKS = 20 * 260;
	private static final int SIGN_RANDOM_COOLDOWN_TICKS = 20 * 280;
	private static final int MAX_SIGNS_PER_CYCLE = 5;
	private static final int THEFT_FIRST_COOLDOWN_TICKS = 20 * 300;
	private static final int THEFT_MIN_COOLDOWN_TICKS = 20 * 420;
	private static final int THEFT_RANDOM_COOLDOWN_TICKS = 20 * 360;
	private static final int MAX_THEFTS_PER_CYCLE = 3;
	private static final int THEFT_HOME_RADIUS = 48;
	private static final int THEFT_VERTICAL_RADIUS = 16;
	private static final int THEFT_MAX_STOLEN_SLOTS = 7;
	private static final double THEFT_PLAYER_MIN_DISTANCE = 72.0D;
	private static final int DISGUISE_FIRST_MIN_COOLDOWN_TICKS = 20 * 70;
	private static final int DISGUISE_FIRST_RANDOM_COOLDOWN_TICKS = 20 * 150;
	private static final int DISGUISE_MIN_COOLDOWN_TICKS = 20 * 420;
	private static final int DISGUISE_RANDOM_COOLDOWN_TICKS = 20 * 420;
	private static final int MAX_DISGUISES_PER_CYCLE = 4;
	private static final double DISGUISE_NEARBY_DISTANCE = 120.0D;
	private static final int EVENT_LOCK_TICKS = 20 * 75;
	private static final double NEARBY_HEROBRINE_DISTANCE = 320.0D;
	private static final double MONITORING_MAX_DISTANCE = 300.0D;
	private static final Direction[] HORIZONTAL_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	private static final Map<UUID, PlayerTracker> TRACKERS = new HashMap<>();
	private static final Random RANDOM = new Random();

	private record TunnelPlan(BlockPos start, Direction direction, int length) {
	}

	private record TheftTarget(BlockPos pos, Container container) {
	}

	private HerobrineSpawnManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(HerobrineSpawnManager::tickServer);
	}

	public static boolean forceDayAndSpawn(ServerPlayer player, int day) {
		if (!isSurvivalPlayer(player)) {
			return false;
		}
		PlayerTracker tracker = getTracker(player);
		tracker.testDayOverride = Math.max(1, Math.min(4, day));
		tracker.cooldownTicks = 0;
		return spawnEncounter(player, tracker.testDayOverride, true);
	}

	public static boolean forceSpawnCurrentStage(ServerPlayer player) {
		if (!isSurvivalPlayer(player)) {
			return false;
		}
		PlayerTracker tracker = getTracker(player);
		return spawnEncounter(player, tracker.getStage(player.level().getGameTime()), true);
	}

	public static boolean forceAppearanceSign(ServerPlayer player) {
		if (!isSurvivalPlayer(player)) {
			return false;
		}
		PlayerTracker tracker = getTracker(player);
		return generateAppearanceSign(player.level(), player, tracker.getStage(player.level().getGameTime()), true, -1);
	}

	public static boolean forceAppearanceSign(ServerPlayer player, int signType) {
		if (!isSurvivalPlayer(player)) {
			return false;
		}
		PlayerTracker tracker = getTracker(player);
		return generateAppearanceSign(player.level(), player, tracker.getStage(player.level().getGameTime()), true, signType);
	}

	public static boolean forceTheft(ServerPlayer player) {
		if (!isSurvivalPlayer(player)) {
			return false;
		}
		PlayerTracker tracker = getTracker(player);
		BlockPos homePos = tracker.knownRespawnPos != null ? tracker.knownRespawnPos : player.blockPosition();
		return attemptTheft(player.level(), player, homePos, true) > 0;
	}

	public static boolean forceDisguise(ServerPlayer player, int requestedType) {
		if (!isSurvivalPlayer(player)) {
			return false;
		}
		return spawnDisguiseEncounter(player, requestedType, true);
	}

	public static void resetTestOverride(ServerPlayer player) {
		PlayerTracker tracker = getTracker(player);
		tracker.testDayOverride = 0;
		tracker.cooldownTicks = FIRST_ATTEMPT_COOLDOWN_TICKS;
	}

	public static void resetCycleAfterDeath(ServerPlayer player) {
		PlayerTracker tracker = getTracker(player);
		tracker.resetCycle(player);
		applyPostDeathCooldown(tracker);
		vanishAllHerobrines(player.level().getServer());
	}

	public static void resetCycleAfterDeathScene(ServerPlayer player) {
		PlayerTracker tracker = getTracker(player);
		tracker.resetCycle(player);
		applyPostDeathCooldown(tracker);
	}

	private static void applyPostDeathCooldown(PlayerTracker tracker) {
		tracker.cooldownTicks = Math.max(tracker.cooldownTicks, POST_DEATH_RESET_COOLDOWN_TICKS);
		tracker.eventLockTicks = Math.max(tracker.eventLockTicks, POST_DEATH_RESET_COOLDOWN_TICKS);
		tracker.ambienceCooldownTicks = Math.max(tracker.ambienceCooldownTicks, POST_DEATH_RESET_COOLDOWN_TICKS);
		tracker.signCooldownTicks = Math.max(tracker.signCooldownTicks, POST_DEATH_RESET_COOLDOWN_TICKS);
		tracker.theftCooldownTicks = Math.max(tracker.theftCooldownTicks, POST_DEATH_RESET_COOLDOWN_TICKS);
		tracker.disguiseCooldownTicks = Math.max(tracker.disguiseCooldownTicks, POST_DEATH_RESET_COOLDOWN_TICKS);
		tracker.postDeathCleanupTicks = Math.max(tracker.postDeathCleanupTicks, 20 * 8);
	}

	public static Component getStatus(ServerPlayer player) {
		PlayerTracker tracker = getTracker(player);
		long gameTime = player.level().getGameTime();
		int stage = tracker.getStage(gameTime);
		int days = tracker.getProgressDays(gameTime);
		String spawnText = tracker.knownRespawnPos == null ? "unknown" : tracker.knownRespawnPos.toShortString();
		String overrideText = tracker.testDayOverride > 0 ? ", test day=" + tracker.testDayOverride : "";
		return Component.literal("Herobrine stage=" + stage + ", progress days=" + days + "/" + tracker.combatDay + ", next attempt=" + Math.max(0, tracker.cooldownTicks / 20) + "s, event lock=" + Math.max(0, tracker.eventLockTicks / 20) + "s, known spawn=" + spawnText + overrideText);
	}

	private static void tickServer(MinecraftServer server) {
		if (server.getTickCount() % 20 != 0) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPlayer(player);
		}
	}

	private static void tickPlayer(ServerPlayer player) {
		PlayerTracker tracker = getTracker(player);
		if (player.isDeadOrDying() || !player.isAlive()) {
			if (tracker.consumeDeathReset(player)) {
				applyPostDeathCooldown(tracker);
			}
			if (!hasDeathLootingHerobrine(player.level().getServer(), player)) {
				vanishAllHerobrines(player.level().getServer());
			}
			return;
		}
		if (player.isCreative() || player.isSpectator()) {
			vanishAllHerobrines(player.level().getServer());
			return;
		}
		if (player.level().getDifficulty() != net.minecraft.world.Difficulty.HARD) {
			vanishAllHerobrines(player.level().getServer());
			return;
		}

		ServerLevel level = player.level();
		if (finishDeathLootHerobrinesAfterRespawn(player.level().getServer(), player)) {
			applyPostDeathCooldown(tracker);
			return;
		}
		if (tracker.postDeathCleanupTicks > 0) {
			tracker.postDeathCleanupTicks -= 20;
			vanishAllHerobrines(player.level().getServer());
			return;
		}
		boolean moving = tracker.tick(player);
		if (tracker.consumeDeathReset(player)) {
			applyPostDeathCooldown(tracker);
			vanishAllHerobrines(player.level().getServer());
			return;
		}

		int stage = tracker.getStage(level.getGameTime());
		double nearestHerobrineDistance = getNearestHerobrineDistance(level, player);
		boolean nearbyHerobrine = nearestHerobrineDistance >= 0.0D;
		boolean nearbyDisguise = hasNearbyDisguise(level, player);
		boolean activeEntityEvent = nearbyHerobrine || nearbyDisguise;
		tracker.tickAmbience(player, stage, nearbyHerobrine, nearestHerobrineDistance);
		tracker.tickSigns(player, stage, moving, activeEntityEvent);
		tracker.tickTheft(player, activeEntityEvent);
		tracker.tickDisguise(player, activeEntityEvent);

		if (activeEntityEvent || tracker.isEventLocked() || hasNearbyHerobrine(level, player) || hasNearbyDisguise(level, player)) {
			tracker.cooldownTicks = Math.max(tracker.cooldownTicks, 20 * 20);
			return;
		}

		if (tracker.cooldownTicks > 0) {
			tracker.cooldownTicks -= 20;
			return;
		}
		if (stage == 4 && tracker.getProgressDays(level.getGameTime()) < tracker.nextCombatAllowedDay) {
			tracker.cooldownTicks = FAILED_ATTEMPT_COOLDOWN_TICKS;
			return;
		}

		double chance = !tracker.hasSpawnedThisCycle ? 1.0D : stage == 1 ? 0.58D : stage == 2 ? 0.68D : stage == 3 ? 0.76D : 0.72D;
		if (RANDOM.nextDouble() <= chance && spawnEncounter(player, stage, false)) {
			if (stage == 4) {
				tracker.nextCombatAllowedDay = tracker.getProgressDays(level.getGameTime()) + POST_COMBAT_WAIT_DAYS;
			}
			tracker.cooldownTicks = switch (stage) {
				case 1 -> DAY_ONE_SPAWN_COOLDOWN_TICKS;
				case 2 -> DAY_TWO_SPAWN_COOLDOWN_TICKS;
				case 3 -> DAY_THREE_SPAWN_COOLDOWN_TICKS;
				default -> COMBAT_SPAWN_COOLDOWN_TICKS;
			};
		} else {
			tracker.cooldownTicks = FAILED_ATTEMPT_COOLDOWN_TICKS;
		}
	}

	private static PlayerTracker getTracker(ServerPlayer player) {
		return TRACKERS.computeIfAbsent(player.getUUID(), uuid -> new PlayerTracker(player));
	}

	private static boolean isSurvivalPlayer(ServerPlayer player) {
		return player.level().getDifficulty() == net.minecraft.world.Difficulty.HARD
			&& player.isAlive()
			&& !player.isDeadOrDying()
			&& !player.isCreative()
			&& !player.isSpectator();
	}

	private static boolean hasNearbyHerobrine(ServerLevel level, ServerPlayer player) {
		return getNearestHerobrineDistance(level, player) >= 0.0D;
	}

	private static boolean hasNearbyDisguise(ServerLevel level, ServerPlayer player) {
		double maxDistanceSqr = DISGUISE_NEARBY_DISTANCE * DISGUISE_NEARBY_DISTANCE;
		for (HerobrineDisguiseSheepEntity sheep : level.getEntities(EntityTypeTest.forClass(HerobrineDisguiseSheepEntity.class), sheep -> sheep.distanceToSqr(player) <= maxDistanceSqr)) {
			return true;
		}
		for (HerobrineDisguiseCowEntity cow : level.getEntities(EntityTypeTest.forClass(HerobrineDisguiseCowEntity.class), cow -> cow.distanceToSqr(player) <= maxDistanceSqr)) {
			return true;
		}
		return false;
	}

	private static boolean hasDeathLootingHerobrine(MinecraftServer server, ServerPlayer player) {
		if (server == null) {
			return false;
		}
		for (ServerLevel level : server.getAllLevels()) {
			for (HerobrineEntity herobrine : level.getEntities(EntityTypeTest.forClass(HerobrineEntity.class), herobrine -> herobrine.isCollectingDeathLootFor(player))) {
				return true;
			}
		}
		return false;
	}

	private static boolean finishDeathLootHerobrinesAfterRespawn(MinecraftServer server, ServerPlayer player) {
		if (server == null) {
			return false;
		}
		boolean finished = false;
		for (ServerLevel level : server.getAllLevels()) {
			for (HerobrineEntity herobrine : level.getEntities(EntityTypeTest.forClass(HerobrineEntity.class), herobrine -> herobrine.isCollectingDeathLootFor(player))) {
				herobrine.finishDeathLootAfterRespawn();
				finished = true;
			}
		}
		return finished;
	}

	private static double getNearestHerobrineDistance(ServerLevel level, ServerPlayer player) {
		double maxDistanceSqr = NEARBY_HEROBRINE_DISTANCE * NEARBY_HEROBRINE_DISTANCE;
		double nearestDistanceSqr = Double.MAX_VALUE;
		for (HerobrineEntity herobrine : level.getEntities(EntityTypeTest.forClass(HerobrineEntity.class), herobrine -> herobrine.distanceToSqr(player) <= maxDistanceSqr)) {
			nearestDistanceSqr = Math.min(nearestDistanceSqr, herobrine.distanceToSqr(player));
		}
		return nearestDistanceSqr == Double.MAX_VALUE ? -1.0D : Math.sqrt(nearestDistanceSqr);
	}

	private static void vanishAllHerobrines(MinecraftServer server) {
		if (server == null) {
			return;
		}
		for (ServerLevel level : server.getAllLevels()) {
			for (HerobrineEntity herobrine : level.getEntities(EntityTypeTest.forClass(HerobrineEntity.class), herobrine -> true)) {
				herobrine.vanishFromWorld();
			}
			for (HerobrineDisguiseSheepEntity sheep : level.getEntities(EntityTypeTest.forClass(HerobrineDisguiseSheepEntity.class), sheep -> true)) {
				sheep.discard();
			}
			for (HerobrineDisguiseCowEntity cow : level.getEntities(EntityTypeTest.forClass(HerobrineDisguiseCowEntity.class), cow -> true)) {
				cow.discard();
			}
		}
	}

	private static boolean spawnEncounter(ServerPlayer player, int stage, boolean forced) {
		ServerLevel level = player.level();
		if (!forced && hasNearbyHerobrine(level, player)) {
			return false;
		}

		BlockPos watchPos = null;
		BlockPos spawnPos;
		if (stage < 4) {
			watchPos = findMonitoringPosition(level, player, stage);
			if (watchPos == null) {
				return false;
			}
			spawnPos = findMonitoringStartPosition(level, player, watchPos, stage);
		} else {
			spawnPos = findCombatEncounterPosition(level, player, stage);
		}
		if (spawnPos == null) {
			return false;
		}

		HerobrineEntity herobrine = new HerobrineEntity(ModEntities.HEROBRINE, level);
		double x = spawnPos.getX() + 0.5D;
		double y = spawnPos.getY();
		double z = spawnPos.getZ() + 0.5D;
		float yaw = getYawToward(player.position(), x, z);
		herobrine.snapTo(x, y, z, yaw, 0.0F);
		if (stage < 4) {
			int stalkingTicks = stage == 1 ? STALK_DAY_ONE_TICKS : stage == 2 ? STALK_DAY_TWO_TICKS : STALK_DAY_THREE_TICKS;
			herobrine.setStalkingMode(stalkingTicks, stage == 3, stage == 3 ? 0.25F : 0.0F, watchPos);
		} else {
			herobrine.setTarget(player);
		}

		boolean added = level.addFreshEntity(herobrine);
		if (added) {
			PlayerTracker tracker = getTracker(player);
			tracker.hasSpawnedThisCycle = true;
			tracker.startEventLock();
			tracker.ambienceCooldownTicks = stage < 4 ? 20 * 4 : tracker.ambienceCooldownTicks;
			level.playSound(null, player.blockPosition(), ModSounds.CAVE_1, SoundSource.HOSTILE, 1.2F, 1.0F);
		}
		return added;
	}

	private static boolean spawnDisguiseEncounter(ServerPlayer player, int requestedType, boolean forced) {
		ServerLevel level = player.level();
		if (!forced && (hasNearbyHerobrine(level, player) || hasNearbyDisguise(level, player))) {
			return false;
		}

		BlockPos spawnPos = findDisguisePosition(level, player, forced);
		if (spawnPos == null) {
			return false;
		}

		int type = requestedType >= 0 ? requestedType : (RANDOM.nextDouble() < 0.70D ? 0 : 1);
		float yaw = getYawToward(player.position(), spawnPos.getX() + 0.5D, spawnPos.getZ() + 0.5D);
		if (type == 1) {
			HerobrineDisguiseCowEntity cow = new HerobrineDisguiseCowEntity(ModEntities.HEROBRINE_COW, level);
			cow.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
			return level.addFreshEntity(cow);
		}

		HerobrineDisguiseSheepEntity sheep = new HerobrineDisguiseSheepEntity(ModEntities.HEROBRINE_SHEEP, level);
		sheep.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
		return level.addFreshEntity(sheep);
	}

	private static BlockPos findDisguisePosition(ServerLevel level, ServerPlayer player, boolean forced) {
		if (forced) {
			Vec3 look = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
			if (look.lengthSqr() < 0.0001D) {
				look = new Vec3(1.0D, 0.0D, 0.0D);
			}
			look = look.normalize();
			Vec3 side = new Vec3(-look.z, 0.0D, look.x);
			for (int distance : new int[]{18, 22, 14, 26, 30}) {
				for (int sideOffset = -5; sideOffset <= 5; sideOffset++) {
					Vec3 pos = player.position().add(look.scale(distance)).add(side.scale(sideOffset));
					BlockPos origin = BlockPos.containing(pos);
					for (int yOffset = 8; yOffset >= -8; yOffset--) {
						BlockPos candidate = origin.offset(0, yOffset, 0);
						if (level.hasChunkAt(candidate) && canStandAt(level, candidate)) {
							return candidate;
						}
					}
				}
			}
		}

		for (int attempt = 0; attempt < 80; attempt++) {
			double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
			double radius = 24.0D + RANDOM.nextDouble() * 28.0D;
			double x = player.getX() + Math.cos(angle) * radius;
			double z = player.getZ() + Math.sin(angle) * radius;
			BlockPos origin = BlockPos.containing(x, player.getY(), z);
			for (int yOffset = 12; yOffset >= -12; yOffset--) {
				BlockPos candidate = origin.offset(0, yOffset, 0);
				if (level.hasChunkAt(candidate) && canStandAt(level, candidate)) {
					return candidate;
				}
			}
		}
		return null;
	}

	private static boolean generateAppearanceSign(ServerLevel level, ServerPlayer player, int stage, boolean forced, int preferredSignType) {
		int firstSign = preferredSignType >= 0 ? Math.max(0, Math.min(2, preferredSignType)) : RANDOM.nextInt(3);
		int attempts = preferredSignType >= 0 ? 1 : 3;
		for (int i = 0; i < attempts; i++) {
			int signType = (firstSign + i) % 3;
			boolean created = switch (signType) {
				case 0 -> tryStripTreeNearPlayer(level, player, forced);
				case 1 -> tryCreateTwoByTwoTunnel(level, player, forced);
				default -> tryCreateSandPyramid(level, player, forced);
			};
			if (created) {
				playAppearanceSignSound(level, player);
				return true;
			}
		}
		return false;
	}

	private static void playAppearanceSignSound(ServerLevel level, ServerPlayer player) {
		level.playSound(null, player.blockPosition(), ModSounds.CAVE_4, SoundSource.HOSTILE, 1.35F, 0.95F + RANDOM.nextFloat() * 0.1F);
	}

	private static int attemptTheft(ServerLevel level, ServerPlayer player, BlockPos homePos, boolean forced) {
		if (homePos == null) {
			return 0;
		}
		if (!forced && player.blockPosition().distSqr(homePos) < THEFT_PLAYER_MIN_DISTANCE * THEFT_PLAYER_MIN_DISTANCE) {
			return 0;
		}

		List<TheftTarget> targets = findValuableContainers(level, homePos);
		if (targets.isEmpty()) {
			return 0;
		}

		while (!targets.isEmpty()) {
			TheftTarget target = targets.remove(RANDOM.nextInt(targets.size()));
			if (spawnTheftGlimpse(level, player, target.pos())) {
				return 1;
			}
		}
		return 0;
	}

	private static List<TheftTarget> findValuableContainers(ServerLevel level, BlockPos homePos) {
		List<TheftTarget> containers = new ArrayList<>();
		for (int y = -THEFT_VERTICAL_RADIUS; y <= THEFT_VERTICAL_RADIUS; y++) {
			for (int x = -THEFT_HOME_RADIUS; x <= THEFT_HOME_RADIUS; x++) {
				for (int z = -THEFT_HOME_RADIUS; z <= THEFT_HOME_RADIUS; z++) {
					if (x * x + z * z > THEFT_HOME_RADIUS * THEFT_HOME_RADIUS) {
						continue;
					}
					BlockPos pos = homePos.offset(x, y, z);
					if (!level.hasChunkAt(pos)) {
						continue;
					}
					BlockEntity blockEntity = level.getBlockEntity(pos);
					if (blockEntity instanceof Container container && containerHasValuables(container)) {
						containers.add(new TheftTarget(pos.immutable(), container));
					}
				}
			}
		}
		return containers;
	}

	private static boolean containerHasValuables(Container container) {
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			if (isValuableForTheft(container.getItem(slot))) {
				return true;
			}
		}
		return false;
	}

	private static int stealFromContainer(Container container, int maxSlots) {
		List<Integer> valuableSlots = new ArrayList<>();
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			if (isValuableForTheft(container.getItem(slot))) {
				valuableSlots.add(slot);
			}
		}

		int stolenSlots = 0;
		while (!valuableSlots.isEmpty() && stolenSlots < maxSlots) {
			int slotIndex = RANDOM.nextInt(valuableSlots.size());
			int slot = valuableSlots.remove(slotIndex);
			ItemStack stack = container.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			int takeCount = getTheftTakeCount(stack);
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

	private static int getTheftTakeCount(ItemStack stack) {
		String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
		if (isSingleValuableItem(path) || stack.getCount() == 1) {
			return 1;
		}
		return Math.max(1, Math.min(8, stack.getCount() / 4));
	}

	private static void playChestTheftCue(ServerLevel level, BlockPos chestPos) {
		BlockState state = level.getBlockState(chestPos);
		level.blockEvent(chestPos, state.getBlock(), 1, 1);
		level.playSound(null, chestPos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.8F, 0.75F);
		level.sendParticles(ParticleTypes.SMOKE, chestPos.getX() + 0.5D, chestPos.getY() + 0.9D, chestPos.getZ() + 0.5D, 10, 0.25D, 0.2D, 0.25D, 0.02D);
		level.blockEvent(chestPos, state.getBlock(), 1, 0);
		level.playSound(null, chestPos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.55F, 0.8F);
	}

	private static boolean spawnTheftGlimpse(ServerLevel level, ServerPlayer player, BlockPos chestPos) {
		BlockPos spawnPos = findTheftApproachPosition(level, chestPos, player.blockPosition());
		if (spawnPos == null) {
			spawnPos = findWorkerPosition(level, chestPos, player.blockPosition());
		}
		if (spawnPos == null) {
			return false;
		}
		HerobrineEntity herobrine = new HerobrineEntity(ModEntities.HEROBRINE, level);
		float yaw = getYawToward(Vec3.atCenterOf(chestPos), spawnPos.getX() + 0.5D, spawnPos.getZ() + 0.5D);
		herobrine.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
		herobrine.setTheftTaskMode(chestPos, 20 * 50);
		boolean spawned = level.addFreshEntity(herobrine);
		if (spawned) {
			level.sendParticles(ParticleTypes.SMOKE, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D, 24, 0.35D, 0.6D, 0.35D, 0.03D);
			level.playSound(null, spawnPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.65F, 0.65F);
		}
		return spawned;
	}

	private static BlockPos findTheftApproachPosition(ServerLevel level, BlockPos chestPos, BlockPos playerPos) {
		Vec3 chestCenter = Vec3.atCenterOf(chestPos);
		Vec3 playerCenter = Vec3.atCenterOf(playerPos);
		Vec3 fromPlayer = chestCenter.subtract(playerCenter).multiply(1.0D, 0.0D, 1.0D);
		if (fromPlayer.lengthSqr() < 1.0D) {
			fromPlayer = new Vec3(1.0D, 0.0D, 0.0D);
		}
		Vec3 towardPlayer = fromPlayer.normalize().scale(-1.0D);

		for (int distance : new int[]{22, 28, 16, 34, 40}) {
			BlockPos origin = BlockPos.containing(chestCenter.add(towardPlayer.scale(distance)));
			for (int side = -5; side <= 5; side++) {
				Vec3 perpendicular = new Vec3(-towardPlayer.z, 0.0D, towardPlayer.x).scale(side);
				BlockPos sideOrigin = BlockPos.containing(Vec3.atCenterOf(origin).add(perpendicular));
				for (int yOffset = 10; yOffset >= -10; yOffset--) {
					BlockPos candidate = sideOrigin.offset(0, yOffset, 0);
					if (level.hasChunkAt(candidate) && canStandAt(level, candidate)) {
						return candidate;
					}
				}
			}
		}
		return null;
	}

	private static boolean isSingleValuableItem(String path) {
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

	private static boolean isValuableForTheft(ItemStack stack) {
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
				|| isSingleValuableItem(path);
	}

	private static BlockPos findCombatEncounterPosition(ServerLevel level, ServerPlayer player, int stage) {
		double minDistance = 12.0D;
		double extraDistance = 8.0D;

		for (int attempt = 0; attempt < 48; attempt++) {
			double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
			double radius = minDistance + RANDOM.nextDouble() * extraDistance;
			double x = player.getX() + Math.cos(angle) * radius;
			double z = player.getZ() + Math.sin(angle) * radius;
			BlockPos origin = BlockPos.containing(x, player.getY(), z);

			for (int yOffset = 10; yOffset >= -10; yOffset--) {
				BlockPos candidate = origin.offset(0, yOffset, 0);
				if (canStandAt(level, candidate)) {
					return candidate;
				}
			}
		}

		return null;
	}

	private static BlockPos findMonitoringPosition(ServerLevel level, ServerPlayer player, int stage) {
		double minDistance = stage == 1 ? 90.0D : stage == 2 ? 70.0D : 45.0D;
		double maxDistance = stage == 1 ? 170.0D : stage == 2 ? 135.0D : 80.0D;
		maxDistance = Math.min(maxDistance, MONITORING_MAX_DISTANCE);

		BlockPos fallback = null;
		for (int attempt = 0; attempt < 96; attempt++) {
			double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
			double radius = minDistance + RANDOM.nextDouble() * Math.max(1.0D, maxDistance - minDistance);
			double x = player.getX() + Math.cos(angle) * radius;
			double z = player.getZ() + Math.sin(angle) * radius;
			BlockPos origin = BlockPos.containing(x, player.getY(), z);

			for (int yOffset = 12; yOffset >= -12; yOffset--) {
				BlockPos candidate = origin.offset(0, yOffset, 0);
				if (!canStandAtMonitoringSpot(level, candidate) || candidate.distSqr(player.blockPosition()) > MONITORING_MAX_DISTANCE * MONITORING_MAX_DISTANCE) {
					continue;
				}
				if (hasCoverTowardPlayer(level, candidate, player.blockPosition())) {
					return candidate;
				}
				if (fallback == null) {
					fallback = candidate;
				}
			}
		}

		return fallback;
	}

	private static BlockPos findMonitoringStartPosition(ServerLevel level, ServerPlayer player, BlockPos watchPos, int stage) {
		double minDistance = stage == 3 ? 20.0D : 26.0D;
		double maxDistance = stage == 3 ? 42.0D : 58.0D;
		double watchDistanceSqr = watchPos.distSqr(player.blockPosition());

		for (int attempt = 0; attempt < 56; attempt++) {
			double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
			double radius = minDistance + RANDOM.nextDouble() * Math.max(1.0D, maxDistance - minDistance);
			double x = watchPos.getX() + Math.cos(angle) * radius;
			double z = watchPos.getZ() + Math.sin(angle) * radius;
			BlockPos origin = BlockPos.containing(x, watchPos.getY(), z);

			for (int yOffset = 10; yOffset >= -10; yOffset--) {
				BlockPos candidate = origin.offset(0, yOffset, 0);
				if (canStandAtMonitoringSpot(level, candidate) && candidate.distSqr(player.blockPosition()) >= watchDistanceSqr) {
					return candidate;
				}
			}
		}

		return watchPos;
	}

	private static boolean canStandAtMonitoringSpot(ServerLevel level, BlockPos pos) {
		if (!canStandAt(level, pos)) {
			return false;
		}
		BlockState floor = level.getBlockState(pos.below());
		return !floor.typeHolder().is(BlockTags.LEAVES) && !floor.typeHolder().is(BlockTags.LOGS);
	}

	private static boolean hasCoverTowardPlayer(ServerLevel level, BlockPos candidate, BlockPos playerPos) {
		int xStep = Integer.compare(playerPos.getX(), candidate.getX());
		int zStep = Integer.compare(playerPos.getZ(), candidate.getZ());
		if (xStep == 0 && zStep == 0) {
			return false;
		}

		for (int step = 1; step <= 3; step++) {
			BlockPos coverPos = candidate.offset(xStep * step, 0, zStep * step);
			if (isMonitoringCover(level, coverPos) || isMonitoringCover(level, coverPos.above())) {
				return true;
			}
		}
		return false;
	}

	private static boolean isMonitoringCover(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || state.liquid()) {
			return false;
		}
		return state.typeHolder().is(BlockTags.LOGS)
				|| state.typeHolder().is(BlockTags.LEAVES)
				|| !state.getCollisionShape(level, pos).isEmpty();
	}

	private static boolean tryStripTreeNearPlayer(ServerLevel level, ServerPlayer player, boolean forced) {
		if (forced) {
			BlockPos directLog = findTreeLogAroundPlayer(level, player, 96, 32);
			if (directLog != null && spawnTreeStripper(level, player, directLog)) {
				return true;
			}
		}

		for (int attempt = 0; attempt < (forced ? 48 : 20); attempt++) {
			BlockPos surface = findSurfaceCandidate(level, player, forced ? 18.0D : 55.0D, forced ? 80.0D : 150.0D);
			if (surface == null) {
				continue;
			}

			BlockPos log = findNearbyTreeLog(level, surface);
			if (log != null && spawnTreeStripper(level, player, log)) {
				return true;
			}
		}
		return false;
	}

	private static BlockPos findTreeLogAroundPlayer(ServerLevel level, ServerPlayer player, int radius, int verticalRange) {
		BlockPos center = player.blockPosition();
		int radiusSqr = radius * radius;
		for (int y = -8; y <= verticalRange; y++) {
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					if (x * x + z * z > radiusSqr) {
						continue;
					}
					BlockPos pos = center.offset(x, y, z);
					if (!level.hasChunkAt(pos)) {
						continue;
					}
					if (level.getBlockState(pos).typeHolder().is(BlockTags.LOGS) && hasLeavesNear(level, pos)) {
						return pos;
					}
				}
			}
		}
		return null;
	}

	private static BlockPos findNearbyTreeLog(ServerLevel level, BlockPos center) {
		for (int y = -2; y <= 10; y++) {
			for (int x = -7; x <= 7; x++) {
				for (int z = -7; z <= 7; z++) {
					BlockPos pos = center.offset(x, y, z);
					if (level.getBlockState(pos).typeHolder().is(BlockTags.LOGS) && hasLeavesNear(level, pos)) {
						return pos;
					}
				}
			}
		}
		return null;
	}

	private static boolean hasLeavesNear(ServerLevel level, BlockPos logPos) {
		for (int y = -1; y <= 6; y++) {
			for (int x = -4; x <= 4; x++) {
				for (int z = -4; z <= 4; z++) {
					if (level.getBlockState(logPos.offset(x, y, z)).typeHolder().is(BlockTags.LEAVES)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean spawnTreeStripper(ServerLevel level, ServerPlayer player, BlockPos logPos) {
		List<BlockPos> leaves = collectLeavesAroundTree(level, logPos);
		if (leaves.size() < 5) {
			return false;
		}
		return spawnBlockTaskHerobrine(level, player, logPos, leaves, List.of());
	}

	private static List<BlockPos> collectLeavesAroundTree(ServerLevel level, BlockPos logPos) {
		List<BlockPos> leaves = new ArrayList<>();
		for (int y = -2; y <= 9; y++) {
			for (int x = -5; x <= 5; x++) {
				for (int z = -5; z <= 5; z++) {
					if (x * x + z * z > 30) {
						continue;
					}
					BlockPos pos = logPos.offset(x, y, z);
					if (level.getBlockState(pos).typeHolder().is(BlockTags.LEAVES)) {
						leaves.add(pos.immutable());
						if (leaves.size() >= 160) {
							return leaves;
						}
					}
				}
			}
		}
		return leaves;
	}

	private static boolean tryCreateTwoByTwoTunnel(ServerLevel level, ServerPlayer player, boolean forced) {
		TunnelPlan tunnelPlan = findMountainTunnelPlan(level, player, forced);
		return tunnelPlan != null && spawnTunnelDigger(level, player, tunnelPlan.start(), tunnelPlan.direction(), tunnelPlan.length());
	}

	private static TunnelPlan findMountainTunnelPlan(ServerLevel level, ServerPlayer player, boolean forced) {
		for (int attempt = 0; attempt < (forced ? 72 : 42); attempt++) {
			BlockPos standPos = findSurfaceCandidate(level, player, forced ? 22.0D : 70.0D, forced ? 120.0D : 190.0D);
			if (standPos == null) {
				continue;
			}

			Direction firstDirection = HORIZONTAL_DIRECTIONS[RANDOM.nextInt(HORIZONTAL_DIRECTIONS.length)];
			for (int i = 0; i < HORIZONTAL_DIRECTIONS.length; i++) {
				Direction direction = HORIZONTAL_DIRECTIONS[(i + directionIndex(firstDirection)) % HORIZONTAL_DIRECTIONS.length];
				int length = 10 + RANDOM.nextInt(7);
				BlockPos start = standPos.relative(direction);
				if (isMountainTunnelEntrance(level, standPos, start, direction) && canCarveTunnel(level, start, direction, length) && hasMountainMassAbove(level, start, direction, length)) {
					return new TunnelPlan(start, direction, length);
				}
			}
		}
		return null;
	}

	private static boolean isMountainTunnelEntrance(ServerLevel level, BlockPos standPos, BlockPos start, Direction direction) {
		if (!canStandAt(level, standPos)) {
			return false;
		}
		Direction right = rightOf(direction);
		int solidFaceBlocks = 0;
		for (int width = 0; width < 2; width++) {
			for (int height = 0; height < 2; height++) {
				BlockPos pos = start.relative(right, width).above(height);
				BlockState state = level.getBlockState(pos);
				if (state.isAir() || state.liquid() || state.hasBlockEntity() || state.getDestroySpeed(level, pos) < 0.0F || state.getCollisionShape(level, pos).isEmpty()) {
					return false;
				}
				solidFaceBlocks++;
			}
		}

		int overhead = 0;
		for (int width = -1; width <= 2; width++) {
			for (int height = 2; height <= 7; height++) {
				BlockPos pos = start.relative(right, width).above(height);
				BlockState state = level.getBlockState(pos);
				if (!state.isAir() && !state.liquid() && !state.getCollisionShape(level, pos).isEmpty()) {
					overhead++;
				}
			}
		}
		return solidFaceBlocks == 4 && overhead >= 14;
	}

	private static boolean canCarveTunnel(ServerLevel level, BlockPos start, Direction direction, int length) {
		Direction right = rightOf(direction);
		int solidBlocks = 0;
		int airBlocks = 0;
		for (int step = 0; step < length; step++) {
			for (int width = 0; width < 2; width++) {
				for (int height = 0; height < 2; height++) {
					BlockPos pos = start.relative(direction, step).relative(right, width).above(height);
					BlockState state = level.getBlockState(pos);
					if (!canTunnelThrough(level, pos, state)) {
						return false;
					}
					if (state.isAir()) {
						airBlocks++;
					} else {
						solidBlocks++;
					}
				}
			}
		}
		return solidBlocks >= length * 3 && airBlocks <= length;
	}

	private static boolean hasMountainMassAbove(ServerLevel level, BlockPos start, Direction direction, int length) {
		Direction right = rightOf(direction);
		int overhead = 0;
		for (int step = 0; step < length; step++) {
			for (int width = 0; width < 2; width++) {
				for (int y = 2; y <= 8; y++) {
					BlockPos pos = start.relative(direction, step).relative(right, width).above(y);
					BlockState state = level.getBlockState(pos);
					if (!state.isAir() && !state.liquid() && !state.getCollisionShape(level, pos).isEmpty()) {
						overhead++;
					}
				}
			}
		}
		return overhead >= length * 10;
	}

	private static boolean spawnTunnelDigger(ServerLevel level, ServerPlayer player, BlockPos start, Direction direction, int length) {
		BlockPos diggerPos = findTunnelDiggerPosition(level, start, direction);
		if (diggerPos == null) {
			return false;
		}

		HerobrineEntity herobrine = new HerobrineEntity(ModEntities.HEROBRINE, level);
		float yaw = getYawToward(Vec3.atCenterOf(start), diggerPos.getX() + 0.5D, diggerPos.getZ() + 0.5D);
		herobrine.snapTo(diggerPos.getX() + 0.5D, diggerPos.getY(), diggerPos.getZ() + 0.5D, yaw, 0.0F);
		herobrine.setTunnelDiggingMode(start, direction, length);
		return level.addFreshEntity(herobrine);
	}

	private static BlockPos findTunnelDiggerPosition(ServerLevel level, BlockPos start, Direction direction) {
		Direction opposite = direction.getOpposite();
		for (int distance = 1; distance <= 5; distance++) {
			BlockPos base = start.relative(opposite, distance);
			for (int yOffset = 1; yOffset >= -1; yOffset--) {
				BlockPos candidate = base.offset(0, yOffset, 0);
				if (canStandAt(level, candidate)) {
					return candidate;
				}
			}
		}
		return null;
	}

	private static boolean tryCreateSandPyramid(ServerLevel level, ServerPlayer player, boolean forced) {
		for (int attempt = 0; attempt < (forced ? 28 : 16); attempt++) {
			BlockPos surface = findSurfaceCandidate(level, player, forced ? 18.0D : 60.0D, forced ? 90.0D : 150.0D);
			if (surface == null) {
				continue;
			}

			if (canPlaceSandPyramid(level, surface)) {
				return spawnPyramidBuilder(level, player, surface);
			}
		}
		return false;
	}

	private static boolean canPlaceSandPyramid(ServerLevel level, BlockPos center) {
		for (int y = 0; y <= 2; y++) {
			int radius = 2 - y;
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos pos = center.offset(x, y, z);
					if (!canReplaceWithSign(level, pos)) {
						return false;
					}
					if (y == 0 && level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static boolean spawnPyramidBuilder(ServerLevel level, ServerPlayer player, BlockPos center) {
		List<HerobrineEntity.BlockPlacement> placements = new ArrayList<>();
		for (int y = 0; y <= 2; y++) {
			int radius = 2 - y;
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					placements.add(new HerobrineEntity.BlockPlacement(center.offset(x, y, z).immutable(), Blocks.SAND.defaultBlockState()));
				}
			}
		}
		return spawnBlockTaskHerobrine(level, player, center, List.of(), placements);
	}

	private static boolean spawnBlockTaskHerobrine(ServerLevel level, ServerPlayer player, BlockPos workCenter, List<BlockPos> breaks, List<HerobrineEntity.BlockPlacement> placements) {
		BlockPos spawnPos = findWorkerPosition(level, workCenter, player.blockPosition());
		if (spawnPos == null) {
			return false;
		}

		HerobrineEntity herobrine = new HerobrineEntity(ModEntities.HEROBRINE, level);
		float yaw = getYawToward(Vec3.atCenterOf(workCenter), spawnPos.getX() + 0.5D, spawnPos.getZ() + 0.5D);
		herobrine.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
		herobrine.setBlockTaskMode(breaks, placements);
		return level.addFreshEntity(herobrine);
	}

	private static BlockPos findWorkerPosition(ServerLevel level, BlockPos workCenter, BlockPos playerPos) {
		for (int radius = 3; radius <= 9; radius++) {
			for (int attempt = 0; attempt < 24; attempt++) {
				double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
				BlockPos origin = BlockPos.containing(
						workCenter.getX() + Math.cos(angle) * radius,
						workCenter.getY(),
						workCenter.getZ() + Math.sin(angle) * radius
				);
				for (int yOffset = 8; yOffset >= -8; yOffset--) {
					BlockPos candidate = origin.offset(0, yOffset, 0);
					if (canStandAt(level, candidate) && candidate.distSqr(playerPos) <= 180.0D * 180.0D) {
						return candidate;
					}
				}
			}
		}
		return null;
	}

	private static BlockPos findSurfaceCandidate(ServerLevel level, ServerPlayer player, double minDistance, double maxDistance) {
		for (int attempt = 0; attempt < 64; attempt++) {
			double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
			double radius = minDistance + RANDOM.nextDouble() * Math.max(1.0D, maxDistance - minDistance);
			double x = player.getX() + Math.cos(angle) * radius;
			double z = player.getZ() + Math.sin(angle) * radius;
			BlockPos origin = BlockPos.containing(x, player.getY(), z);
			if (!level.hasChunkAt(origin)) {
				continue;
			}

			for (int yOffset = 24; yOffset >= -40; yOffset--) {
				BlockPos candidate = origin.offset(0, yOffset, 0);
				if (canStandAt(level, candidate)) {
					return candidate;
				}
			}
		}
		return null;
	}

	private static boolean canTunnelThrough(ServerLevel level, BlockPos pos, BlockState state) {
		if (state.liquid() || state.hasBlockEntity()) {
			return false;
		}
		return state.isAir() || state.getDestroySpeed(level, pos) >= 0.0F;
	}

	private static boolean canReplaceWithSign(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.liquid() || state.hasBlockEntity()) {
			return false;
		}
		return state.isAir() || state.getCollisionShape(level, pos).isEmpty();
	}

	private static Direction rightOf(Direction direction) {
		return switch (direction) {
			case NORTH -> Direction.EAST;
			case SOUTH -> Direction.WEST;
			case EAST -> Direction.SOUTH;
			case WEST -> Direction.NORTH;
			default -> Direction.EAST;
		};
	}

	private static int directionIndex(Direction direction) {
		for (int i = 0; i < HORIZONTAL_DIRECTIONS.length; i++) {
			if (HORIZONTAL_DIRECTIONS[i] == direction) {
				return i;
			}
		}
		return 0;
	}

	private static boolean canStandAt(ServerLevel level, BlockPos pos) {
		BlockState feet = level.getBlockState(pos);
		BlockState head = level.getBlockState(pos.above());
		BlockState floor = level.getBlockState(pos.below());
		return feet.getCollisionShape(level, pos).isEmpty()
				&& head.getCollisionShape(level, pos.above()).isEmpty()
				&& !floor.getCollisionShape(level, pos.below()).isEmpty();
	}

	private static float getYawToward(Vec3 target, double x, double z) {
		double dx = target.x - x;
		double dz = target.z - z;
		return (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
	}

	private static final class PlayerTracker {
		private long cycleStartGameDay;
		private int sleepDays;
		private int cooldownTicks = FIRST_ATTEMPT_COOLDOWN_TICKS;
		private int testDayOverride;
		private int combatDay;
		private int nextCombatAllowedDay;
		private int ambienceCooldownTicks = 20 * 40;
		private int signCooldownTicks = SIGN_FIRST_COOLDOWN_TICKS;
		private int signsThisCycle;
		private int theftCooldownTicks = THEFT_FIRST_COOLDOWN_TICKS;
		private int theftsThisCycle;
		private int disguiseCooldownTicks = DISGUISE_FIRST_MIN_COOLDOWN_TICKS + RANDOM.nextInt(DISGUISE_FIRST_RANDOM_COOLDOWN_TICKS);
		private int disguisesThisCycle;
		private int eventLockTicks;
		private double lastAmbienceHerobrineDistance = -1.0D;
		private boolean hasSpawnedThisCycle;
		private boolean wasSleeping;
		private boolean wasAlive = true;
		private int postDeathCleanupTicks;
		private BlockPos knownRespawnPos;
		private double lastX;
		private double lastZ;

		private PlayerTracker(ServerPlayer player) {
			this.cycleStartGameDay = player.level().getGameTime() / DAY_TICKS;
			this.combatDay = randomCombatDay();
			this.knownRespawnPos = getRespawnPos(player);
			this.lastX = player.getX();
			this.lastZ = player.getZ();
		}

		private boolean tick(ServerPlayer player) {
			boolean sleeping = player.isSleeping();
			if (this.wasSleeping && !sleeping) {
				this.sleepDays++;
			}
			this.wasSleeping = sleeping;
			if (this.eventLockTicks > 0) {
				this.eventLockTicks -= 20;
			}

			BlockPos respawnPos = getRespawnPos(player);
			if (respawnPos != null) {
				this.knownRespawnPos = respawnPos;
			}

			double movedX = player.getX() - this.lastX;
			double movedZ = player.getZ() - this.lastZ;
			this.lastX = player.getX();
			this.lastZ = player.getZ();
			return movedX * movedX + movedZ * movedZ > 0.02D;
		}

		private boolean consumeDeathReset(ServerPlayer player) {
			boolean alive = player.isAlive() && !player.isDeadOrDying();
			boolean died = this.wasAlive && !alive;
			this.wasAlive = alive;
			if (died) {
				this.resetCycle(player);
				return true;
			}
			return false;
		}

		private void resetCycle(ServerPlayer player) {
			this.cycleStartGameDay = player.level().getGameTime() / DAY_TICKS;
			this.sleepDays = 0;
			this.cooldownTicks = FIRST_ATTEMPT_COOLDOWN_TICKS;
			this.testDayOverride = 0;
			this.combatDay = randomCombatDay();
			this.nextCombatAllowedDay = 0;
			this.ambienceCooldownTicks = 20 * 40;
			this.signCooldownTicks = SIGN_FIRST_COOLDOWN_TICKS;
			this.signsThisCycle = 0;
			this.theftCooldownTicks = THEFT_FIRST_COOLDOWN_TICKS;
			this.theftsThisCycle = 0;
			this.disguiseCooldownTicks = DISGUISE_FIRST_MIN_COOLDOWN_TICKS + RANDOM.nextInt(DISGUISE_FIRST_RANDOM_COOLDOWN_TICKS);
			this.disguisesThisCycle = 0;
			this.eventLockTicks = 0;
			this.lastAmbienceHerobrineDistance = -1.0D;
			this.hasSpawnedThisCycle = false;
			this.wasAlive = player.isAlive() && !player.isDeadOrDying();
			this.knownRespawnPos = getRespawnPos(player);
		}

		private void tickAmbience(ServerPlayer player, int stage, boolean nearbyHerobrine, double nearestHerobrineDistance) {
			if (stage > 3 || player.isSleeping() || !this.hasSpawnedThisCycle || !nearbyHerobrine) {
				this.lastAmbienceHerobrineDistance = -1.0D;
				return;
			}
			if (this.lastAmbienceHerobrineDistance < 0.0D) {
				this.lastAmbienceHerobrineDistance = nearestHerobrineDistance;
				return;
			}
			if (this.ambienceCooldownTicks > 0) {
				this.ambienceCooldownTicks -= 20;
				if (nearestHerobrineDistance > this.lastAmbienceHerobrineDistance + 6.0D) {
					this.lastAmbienceHerobrineDistance = nearestHerobrineDistance;
				}
				return;
			}
			if (nearestHerobrineDistance > this.lastAmbienceHerobrineDistance - AMBIENCE_APPROACH_DISTANCE) {
				if (nearestHerobrineDistance > this.lastAmbienceHerobrineDistance) {
					this.lastAmbienceHerobrineDistance = nearestHerobrineDistance;
				}
				return;
			}

			SoundEvent sound = RANDOM.nextBoolean() ? ModSounds.CAVE_3 : ModSounds.CAVE_6;
			player.level().playSound(null, player.blockPosition(), sound, SoundSource.HOSTILE, 0.95F, 0.95F + RANDOM.nextFloat() * 0.1F);
			this.lastAmbienceHerobrineDistance = nearestHerobrineDistance;
			this.ambienceCooldownTicks = AMBIENCE_MIN_COOLDOWN_TICKS + RANDOM.nextInt(AMBIENCE_RANDOM_COOLDOWN_TICKS);
		}

		private void tickSigns(ServerPlayer player, int stage, boolean moving, boolean nearbyHerobrine) {
			if (player.isSleeping() || nearbyHerobrine || this.isEventLocked() || this.signsThisCycle >= MAX_SIGNS_PER_CYCLE) {
				return;
			}
			if (this.signCooldownTicks > 0) {
				this.signCooldownTicks -= 20;
				return;
			}
			if (!moving && RANDOM.nextDouble() > 0.25D) {
				this.signCooldownTicks = 20 * 40;
				return;
			}

			double chance = stage == 1 ? 0.22D : stage == 2 ? 0.32D : stage == 3 ? 0.42D : 0.28D;
			if (RANDOM.nextDouble() <= chance && generateAppearanceSign(player.level(), player, stage, false, -1)) {
				this.signsThisCycle++;
				this.startEventLock();
			}
			this.signCooldownTicks = SIGN_MIN_COOLDOWN_TICKS + RANDOM.nextInt(SIGN_RANDOM_COOLDOWN_TICKS);
		}

		private void tickTheft(ServerPlayer player, boolean nearbyHerobrine) {
			if (nearbyHerobrine || player.isSleeping() || this.isEventLocked() || this.knownRespawnPos == null || this.theftsThisCycle >= MAX_THEFTS_PER_CYCLE) {
				return;
			}
			if (this.theftCooldownTicks > 0) {
				this.theftCooldownTicks -= 20;
				return;
			}
			int stolenSlots = attemptTheft(player.level(), player, this.knownRespawnPos, false);
			if (stolenSlots > 0) {
				this.theftsThisCycle++;
				this.startEventLock();
			}
			this.theftCooldownTicks = THEFT_MIN_COOLDOWN_TICKS + RANDOM.nextInt(THEFT_RANDOM_COOLDOWN_TICKS);
		}

		private void tickDisguise(ServerPlayer player, boolean blockedByPresence) {
			if (blockedByPresence || player.isSleeping() || this.isEventLocked() || this.disguisesThisCycle >= MAX_DISGUISES_PER_CYCLE) {
				return;
			}
			if (this.disguiseCooldownTicks > 0) {
				this.disguiseCooldownTicks -= 20;
				return;
			}

			if (RANDOM.nextDouble() <= 0.55D && spawnDisguiseEncounter(player, -1, false)) {
				this.disguisesThisCycle++;
				this.startEventLock();
				this.disguiseCooldownTicks = DISGUISE_MIN_COOLDOWN_TICKS + RANDOM.nextInt(DISGUISE_RANDOM_COOLDOWN_TICKS);
			} else {
				this.disguiseCooldownTicks = 20 * 90 + RANDOM.nextInt(20 * 90);
			}
		}

		private boolean isEventLocked() {
			return this.eventLockTicks > 0;
		}

		private void startEventLock() {
			this.eventLockTicks = Math.max(this.eventLockTicks, EVENT_LOCK_TICKS);
		}

		private int getStage(long gameTime) {
			if (this.testDayOverride > 0) {
				return this.testDayOverride;
			}
			int progressDays = this.getProgressDays(gameTime);
			if (progressDays >= this.combatDay) {
				return 4;
			}
			if (progressDays >= 3) {
				return 3;
			}
			if (progressDays >= 1) {
				return 2;
			}
			return 1;
		}

		private int getProgressDays(long gameTime) {
			long naturalDays = Math.max(0L, gameTime / DAY_TICKS - this.cycleStartGameDay);
			return (int) Math.min(100L, naturalDays + this.sleepDays);
		}

		private static int randomCombatDay() {
			return MIN_COMBAT_DAY + RANDOM.nextInt(MAX_COMBAT_DAY - MIN_COMBAT_DAY + 1);
		}

		private static BlockPos getRespawnPos(ServerPlayer player) {
			try {
				return player.getRespawnConfig().respawnData().pos();
			} catch (RuntimeException exception) {
				return player.blockPosition();
			}
		}
	}
}
