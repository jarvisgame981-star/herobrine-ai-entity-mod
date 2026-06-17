package com.engai.herobrine.command;

import com.engai.herobrine.entity.HerobrineEntity;
import com.engai.herobrine.registry.ModEntities;
import com.engai.herobrine.registry.ModSounds;
import com.engai.herobrine.world.HerobrineSpawnManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public final class HerobrineCommands {
	private HerobrineCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("herobrine")
						.requires(source -> source.isPlayer())
						.then(Commands.literal("spawn")
								.executes(context -> {
									ServerPlayer player = context.getSource().getPlayerOrException();
									if (player.isCreative() || player.isSpectator()) {
										context.getSource().sendSuccess(() -> Component.literal("Herobrine only appears in survival."), false);
										return Command.SINGLE_SUCCESS;
									}
									ServerLevel level = context.getSource().getLevel();
									Vec3 spawnPos = player.position().add(player.getLookAngle().scale(3.0D));
									HerobrineEntity herobrine = new HerobrineEntity(ModEntities.HEROBRINE, level);
									herobrine.snapTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot() + 180.0F, 0.0F);
									herobrine.setHealth(HerobrineEntity.MAX_HEALTH_POINTS);
									level.addFreshEntity(herobrine);
									level.playSound(null, player.blockPosition(), ModSounds.CAVE_1, SoundSource.HOSTILE, 1.2F, 1.0F);
									context.getSource().sendSuccess(() -> Component.literal("Herobrine has entered the world."), true);
									return Command.SINGLE_SUCCESS;
								})
						)
						.then(Commands.literal("distance")
								.executes(context -> {
									ServerPlayer player = context.getSource().getPlayerOrException();
									ServerLevel level = context.getSource().getLevel();
									context.getSource().sendSuccess(() -> getNearestHerobrineDistanceMessage(player, level), false);
									return Command.SINGLE_SUCCESS;
								})
						)
						.then(Commands.literal("ai")
								.executes(context -> {
									ServerPlayer player = context.getSource().getPlayerOrException();
									ServerLevel level = context.getSource().getLevel();
									context.getSource().sendSuccess(() -> getNearestHerobrineAiMessage(player, level), false);
									return Command.SINGLE_SUCCESS;
								})
						)
						.then(Commands.literal("steal")
								.executes(context -> forceTheft(context.getSource().getPlayerOrException(), true))
						)
						.then(Commands.literal("disguise")
								.executes(context -> forceDisguise(context.getSource().getPlayerOrException(), -1, "random"))
								.then(Commands.literal("sheep")
										.executes(context -> forceDisguise(context.getSource().getPlayerOrException(), 0, "sheep"))
								)
								.then(Commands.literal("cow")
										.executes(context -> forceDisguise(context.getSource().getPlayerOrException(), 1, "cow"))
								)
						)
						.then(Commands.literal("test")
								.then(Commands.literal("day")
										.then(Commands.argument("day", IntegerArgumentType.integer(1, 4))
												.executes(context -> {
													ServerPlayer player = context.getSource().getPlayerOrException();
													int day = IntegerArgumentType.getInteger(context, "day");
													boolean spawned = HerobrineSpawnManager.forceDayAndSpawn(player, day);
													context.getSource().sendSuccess(() -> Component.literal(spawned ? "Forced Herobrine day " + day + " encounter." : "Could not find a safe place for Herobrine."), true);
													return Command.SINGLE_SUCCESS;
												})
										)
								)
								.then(Commands.literal("force")
										.executes(context -> {
											ServerPlayer player = context.getSource().getPlayerOrException();
											boolean spawned = HerobrineSpawnManager.forceSpawnCurrentStage(player);
											context.getSource().sendSuccess(() -> Component.literal(spawned ? "Forced current Herobrine encounter." : "Could not find a safe place for Herobrine."), true);
											return Command.SINGLE_SUCCESS;
										})
								)
								.then(Commands.literal("sign")
										.executes(context -> {
											ServerPlayer player = context.getSource().getPlayerOrException();
											boolean created = HerobrineSpawnManager.forceAppearanceSign(player);
											context.getSource().sendSuccess(() -> Component.literal(created ? "Forced a Herobrine sign nearby." : "Could not find a safe place for a Herobrine sign."), true);
											return Command.SINGLE_SUCCESS;
										})
										.then(Commands.literal("tree")
												.executes(context -> forceSign(context.getSource().getPlayerOrException(), 0, "tree"))
										)
										.then(Commands.literal("tunnel")
												.executes(context -> forceSign(context.getSource().getPlayerOrException(), 1, "tunnel"))
										)
										.then(Commands.literal("pyramid")
												.executes(context -> forceSign(context.getSource().getPlayerOrException(), 2, "pyramid"))
										)
								)
								.then(Commands.literal("steal")
										.executes(context -> forceTheft(context.getSource().getPlayerOrException(), true))
								)
								.then(Commands.literal("status")
										.executes(context -> {
											ServerPlayer player = context.getSource().getPlayerOrException();
											context.getSource().sendSuccess(() -> HerobrineSpawnManager.getStatus(player), false);
											return Command.SINGLE_SUCCESS;
										})
								)
								.then(Commands.literal("reset")
										.executes(context -> {
											ServerPlayer player = context.getSource().getPlayerOrException();
											HerobrineSpawnManager.resetTestOverride(player);
											context.getSource().sendSuccess(() -> Component.literal("Herobrine test override cleared."), true);
											return Command.SINGLE_SUCCESS;
										})
								)
						)
				)
		);
	}

	private static int forceSign(ServerPlayer player, int signType, String name) {
		boolean created = HerobrineSpawnManager.forceAppearanceSign(player, signType);
		player.sendSystemMessage(Component.literal(created ? "Forced a Herobrine " + name + " sign nearby." : "Could not find a safe place for a Herobrine " + name + " sign."));
		return Command.SINGLE_SUCCESS;
	}

	private static int forceTheft(ServerPlayer player, boolean showResult) {
		boolean stolen = HerobrineSpawnManager.forceTheft(player);
		if (showResult) {
			player.sendSystemMessage(Component.literal(stolen ? "Forced a Herobrine theft." : "Could not find valuable items to steal near your known spawn or current position."));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int forceDisguise(ServerPlayer player, int requestedType, String name) {
		boolean spawned = HerobrineSpawnManager.forceDisguise(player, requestedType);
		player.sendSystemMessage(Component.literal(spawned ? "Forced a Herobrine " + name + " disguise." : "Could not find a safe place for a Herobrine disguise."));
		return Command.SINGLE_SUCCESS;
	}

	private static Component getNearestHerobrineDistanceMessage(ServerPlayer player, ServerLevel level) {
		HerobrineEntity nearest = getNearestHerobrine(player, level);
		if (nearest == null) {
			return Component.literal("No Herobrine found in this dimension.");
		}

		double x = nearest.getX() - player.getX();
		double y = nearest.getY() - player.getY();
		double z = nearest.getZ() - player.getZ();
		double horizontal = Math.sqrt(x * x + z * z);
		double total = Math.sqrt(x * x + y * y + z * z);
		String vertical = y > 0.5D ? "above you" : y < -0.5D ? "below you" : "same height";
		return Component.literal(String.format(Locale.ROOT, "Nearest Herobrine: total %.1f blocks, horizontal %.1f, vertical %.1f (%s).", total, horizontal, Math.abs(y), vertical));
	}

	private static Component getNearestHerobrineAiMessage(ServerPlayer player, ServerLevel level) {
		HerobrineEntity nearest = getNearestHerobrine(player, level);
		if (nearest == null) {
			return Component.literal("No Herobrine found in this dimension.");
		}
		return Component.literal(nearest.getAiDebugInfo(player));
	}

	private static HerobrineEntity getNearestHerobrine(ServerPlayer player, ServerLevel level) {
		var herobrines = level.getEntities(EntityTypeTest.forClass(HerobrineEntity.class), herobrine -> true);
		if (herobrines.isEmpty()) {
			return null;
		}

		HerobrineEntity nearest = null;
		double nearestDistanceSqr = Double.MAX_VALUE;
		for (HerobrineEntity herobrine : herobrines) {
			double distanceSqr = herobrine.distanceToSqr(player);
			if (distanceSqr < nearestDistanceSqr) {
				nearest = herobrine;
				nearestDistanceSqr = distanceSqr;
			}
		}
		return nearest;
	}
}
