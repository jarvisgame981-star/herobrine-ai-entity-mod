package com.engai.herobrine.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class HerobrineChaseGoal extends Goal {
	private final HerobrineEntity herobrine;

	public HerobrineChaseGoal(HerobrineEntity herobrine) {
		this.herobrine = herobrine;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return this.herobrine.getTarget() != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.herobrine.getTarget() != null;
	}

	@Override
	public void tick() {
		LivingEntity target = this.herobrine.getTarget();
		if (target == null) {
			return;
		}

		if (this.herobrine.isBusyWithManualCombatTask()) {
			this.herobrine.moveTowardCombatTarget(target);
			return;
		}

		this.herobrine.getLookControl().setLookAt(target, 30.0F, 30.0F);
		if (this.herobrine.distanceTo(target) > HerobrineEntity.ATTACK_RANGE) {
			this.herobrine.moveTowardCombatTarget(target);
		} else {
			this.herobrine.getNavigation().stop();
		}
	}
}
