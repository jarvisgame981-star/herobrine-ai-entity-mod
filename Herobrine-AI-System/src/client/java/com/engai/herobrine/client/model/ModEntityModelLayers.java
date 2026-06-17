package com.engai.herobrine.client.model;

import com.engai.herobrine.ClassicHerobrineMod;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public final class ModEntityModelLayers {
	public static final ModelLayerLocation HEROBRINE = new ModelLayerLocation(
			Identifier.fromNamespaceAndPath(ClassicHerobrineMod.MOD_ID, "herobrine"),
			"main"
	);

	private ModEntityModelLayers() {
	}

	public static void register() {
		ModelLayerRegistry.registerModelLayer(HEROBRINE, HerobrineModel::createBodyLayer);
	}
}
