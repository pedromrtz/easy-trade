package com.pedromartz.easytrade.network;

import com.pedromartz.easytrade.EasyTradeMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RequestLockedTradesPayload(int containerId, int requestId) implements CustomPacketPayload {
	public static final Type<RequestLockedTradesPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(EasyTradeMod.MOD_ID, "request_locked_trades"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestLockedTradesPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.CONTAINER_ID,
		RequestLockedTradesPayload::containerId,
		ByteBufCodecs.VAR_INT,
		RequestLockedTradesPayload::requestId,
		RequestLockedTradesPayload::new
	);

	@Override
	public Type<RequestLockedTradesPayload> type() {
		return TYPE;
	}
}
