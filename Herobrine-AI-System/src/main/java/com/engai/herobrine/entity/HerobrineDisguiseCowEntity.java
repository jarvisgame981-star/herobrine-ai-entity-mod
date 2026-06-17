package com.engai.herobrine.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class HerobrineDisguiseCowEntity extends Cow {
	private final HerobrineDisguiseController disguiseController = new HerobrineDisguiseController(this);

	public HerobrineDisguiseCowEntity(EntityType<? extends HerobrineDisguiseCowEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 26.0F));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level() instanceof ServerLevel serverLevel) {
			this.disguiseController.tick(serverLevel);
		}
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
		this.disguiseController.hurt(serverLevel, damageSource);
		return false;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (this.level() instanceof ServerLevel serverLevel) {
			this.disguiseController.hurt(serverLevel, this.damageSources().playerAttack(player));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected boolean shouldDropLoot(ServerLevel serverLevel) {
		return false;
	}

	@Override
	public boolean shouldDropExperience() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}
}
