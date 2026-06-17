package com.engai.herobrine.registry;

import com.engai.herobrine.ClassicHerobrineMod;
import com.engai.herobrine.entity.HerobrineDisguiseCowEntity;
import com.engai.herobrine.entity.HerobrineDisguiseSheepEntity;
import com.engai.herobrine.entity.HerobrineEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.sheep.Sheep;

public final class ModEntities {
	public static final EntityType<HerobrineEntity> HEROBRINE = register(
			"herobrine",
			EntityType.Builder.of(HerobrineEntity::new, MobCategory.MONSTER)
					.sized(0.6F, 1.8F)
					.clientTrackingRange(80)
	);
	public static final EntityType<HerobrineDisguiseSheepEntity> HEROBRINE_SHEEP = register(
			"herobrine_sheep",
			EntityType.Builder.of(HerobrineDisguiseSheepEntity::new, MobCategory.CREATURE)
					.sized(0.9F, 1.3F)
					.clientTrackingRange(80)
	);
	public static final EntityType<HerobrineDisguiseCowEntity> HEROBRINE_COW = register(
			"herobrine_cow",
			EntityType.Builder.of(HerobrineDisguiseCowEntity::new, MobCategory.CREATURE)
					.sized(0.9F, 1.4F)
					.clientTrackingRange(80)
	);

	private ModEntities() {
	}

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(
				Registries.ENTITY_TYPE,
				Identifier.fromNamespaceAndPath(ClassicHerobrineMod.MOD_ID, name)
		);
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void register() {
		ClassicHerobrineMod.LOGGER.info("Registering Herobrine entity");
	}

	public static void registerAttributes() {
		FabricDefaultAttributeRegistry.register(HEROBRINE, HerobrineEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(HEROBRINE_SHEEP, Sheep.createAttributes());
		FabricDefaultAttributeRegistry.register(HEROBRINE_COW, AbstractCow.createAttributes());
	}
}
