package com.engai.herobrine.client;

import com.engai.herobrine.ClassicHerobrineMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class HerobrinePortalOverlay {
	private static final Identifier ID = Identifier.fromNamespaceAndPath(ClassicHerobrineMod.MOD_ID, "portal_overlay");
	private static final Identifier NETHER_PORTAL_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/block/nether_portal.png");
	private static int ticksRemaining;
	private static int totalTicks = 1;
	private static float intensity;

	private HerobrinePortalOverlay() {
	}

	public static void register() {
		HudElementRegistry.addLast(ID, HerobrinePortalOverlay::render);
		ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
	}

	public static void show(int durationTicks, float effectIntensity) {
		totalTicks = Math.max(1, durationTicks);
		ticksRemaining = Math.max(ticksRemaining, durationTicks);
		intensity = Math.max(intensity, Mth.clamp(effectIntensity, 0.0F, 1.0F));
		forceVanillaPortalEffect();
	}

	private static void tick() {
		if (ticksRemaining > 0) {
			forceVanillaPortalEffect();
			ticksRemaining--;
			if (ticksRemaining <= 0) {
				intensity = 0.0F;
			}
		}
	}

	private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		// The vanilla portal camera effect already draws the portal overlay.
		// Drawing another texture here made the screen look doubled.
	}

	private static void forceVanillaPortalEffect() {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player != null) {
			float targetIntensity = Mth.clamp(intensity, 0.0F, 0.55F);
			player.oPortalEffectIntensity = Math.max(player.oPortalEffectIntensity, targetIntensity);
			player.portalEffectIntensity = Math.max(player.portalEffectIntensity, targetIntensity);
		}
	}

	private static int argb(int alpha, int red, int green, int blue) {
		return ((alpha & 255) << 24) | ((red & 255) << 16) | ((green & 255) << 8) | (blue & 255);
	}
}
