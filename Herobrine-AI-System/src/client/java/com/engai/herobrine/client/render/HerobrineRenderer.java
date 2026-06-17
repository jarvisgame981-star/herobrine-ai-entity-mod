package com.engai.herobrine.client.render;

import com.engai.herobrine.ClassicHerobrineMod;
import com.engai.herobrine.client.model.HerobrineModel;
import com.engai.herobrine.client.model.ModEntityModelLayers;
import com.engai.herobrine.entity.HerobrineEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

public class HerobrineRenderer extends HumanoidMobRenderer<HerobrineEntity, HerobrineRenderState, HerobrineModel> {
	private static final Identifier TEXTURE_BEARD = Identifier.fromNamespaceAndPath(ClassicHerobrineMod.MOD_ID, "textures/entity/herobrine.png");
	private static final Identifier TEXTURE_NO_BEARD = Identifier.fromNamespaceAndPath(ClassicHerobrineMod.MOD_ID, "textures/entity/classic.png");

	public HerobrineRenderer(EntityRendererProvider.Context context) {
		super(context, new HerobrineModel(context.bakeLayer(ModEntityModelLayers.HEROBRINE)), 0.5F);
	}

	@Override
	public HerobrineRenderState createRenderState() {
		return new HerobrineRenderState();
	}

	@Override
	public void extractRenderState(HerobrineEntity entity, HerobrineRenderState state, float tickProgress) {
		super.extractRenderState(entity, state, tickProgress);
		// نختار سكن ثابت لكل entity حسب الـ UUID بتاعه (نفس الهيروبراين دايما بنفس السكن طول ما هو موجود)
		state.useNoBeardTexture = (entity.getUUID().getLeastSignificantBits() % 2 == 0);
	}

	@Override
	public Identifier getTextureLocation(HerobrineRenderState state) {
		return state.useNoBeardTexture ? TEXTURE_NO_BEARD : TEXTURE_BEARD;
	}
}
