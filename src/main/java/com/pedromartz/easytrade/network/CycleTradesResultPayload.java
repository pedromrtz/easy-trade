package com.pedromartz.easytrade.network;

import com.pedromartz.easytrade.EasyTradeMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CycleTradesResultPayload(int containerId, int requestId, boolean cycled) implements CustomPacketPayload {
	public static final Type<CycleTradesResultPayload> TYPE = new Type<>(EasyTradeMod.id("cycle_trades_result"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CycleTradesResultPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.CONTAINER_ID, CycleTradesResultPayload::containerId,
		ByteBufCodecs.VAR_INT, CycleTradesResultPayload::requestId,
		ByteBufCodecs.BOOL, CycleTradesResultPayload::cycled,
		CycleTradesResultPayload::new
	);

	@Override
	public Type<CycleTradesResultPayload> type() {
		return TYPE;
	}
}
