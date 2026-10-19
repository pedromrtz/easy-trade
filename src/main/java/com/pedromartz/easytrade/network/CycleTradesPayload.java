package com.pedromartz.easytrade.network;

import com.pedromartz.easytrade.EasyTradeMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CycleTradesPayload(int containerId, int requestId) implements CustomPacketPayload {
	public static final Type<CycleTradesPayload> TYPE = new Type<>(EasyTradeMod.id("cycle_trades"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CycleTradesPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.CONTAINER_ID, CycleTradesPayload::containerId,
		ByteBufCodecs.VAR_INT, CycleTradesPayload::requestId,
		CycleTradesPayload::new
	);

	@Override
	public Type<CycleTradesPayload> type() {
		return TYPE;
	}
}
