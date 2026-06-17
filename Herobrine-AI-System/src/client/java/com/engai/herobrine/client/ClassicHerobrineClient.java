package com.engai.herobrine.client;

import com.engai.herobrine.client.model.ModEntityModelLayers;
import com.engai.herobrine.client.render.HerobrineDisguiseCowRenderer;
import com.engai.herobrine.client.render.HerobrineDisguiseSheepRenderer;
import com.engai.herobrine.client.render.HerobrineRenderer;
import com.engai.herobrine.network.HerobrineVisualEffectPayload;
import com.engai.herobrine.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class ClassicHerobrineClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModEntityModelLayers.register();
		EntityRenderers.register(ModEntities.HEROBRINE, HerobrineRenderer::new);
		EntityRenderers.register(ModEntities.HEROBRINE_SHEEP, HerobrineDisguiseSheepRenderer::new);
		EntityRenderers.register(ModEntities.HEROBRINE_COW, HerobrineDisguiseCowRenderer::new);
		HerobrinePortalOverlay.register();
		ClientPlayNetworking.registerGlobalReceiver(HerobrineVisualEffectPayload.TYPE, (payload, context) ->
				context.client().execute(() -> HerobrinePortalOverlay.show(payload.durationTicks(), payload.intensity())));
	}
}
