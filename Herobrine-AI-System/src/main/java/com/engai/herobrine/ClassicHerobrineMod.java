package com.engai.herobrine;

import com.engai.herobrine.command.HerobrineCommands;
import com.engai.herobrine.network.HerobrineVisualEffectPayload;
import com.engai.herobrine.registry.ModEntities;
import com.engai.herobrine.registry.ModSounds;
import com.engai.herobrine.world.HerobrineSpawnManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassicHerobrineMod implements ModInitializer {
	public static final String MOD_ID = "classic_herobrine";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.clientboundPlay().register(HerobrineVisualEffectPayload.TYPE, HerobrineVisualEffectPayload.CODEC);
		ModEntities.register();
		ModEntities.registerAttributes();
		ModSounds.register();
		HerobrineSpawnManager.register();
		HerobrineCommands.register();
		LOGGER.info("The Classic Herobrine is watching.");
	}
}
