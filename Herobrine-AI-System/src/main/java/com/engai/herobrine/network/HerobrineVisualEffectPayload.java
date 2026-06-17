package com.engai.herobrine.network;

import com.engai.herobrine.ClassicHerobrineMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HerobrineVisualEffectPayload(int durationTicks, float intensity) implements CustomPacketPayload {
	public static final Type<HerobrineVisualEffectPayload> TYPE =
			CustomPacketPayload.createType(ClassicHerobrineMod.MOD_ID + "/portal_overlay");
	public static final StreamCodec<RegistryFriendlyByteBuf, HerobrineVisualEffectPayload> CODEC =
			CustomPacketPayload.codec(HerobrineVisualEffectPayload::write, HerobrineVisualEffectPayload::new);

	private HerobrineVisualEffectPayload(RegistryFriendlyByteBuf buf) {
		this(buf.readVarInt(), buf.readFloat());
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeVarInt(this.durationTicks);
		buf.writeFloat(this.intensity);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
