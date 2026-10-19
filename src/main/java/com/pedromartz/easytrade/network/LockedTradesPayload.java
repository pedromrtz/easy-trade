package com.pedromartz.easytrade.network;

import com.pedromartz.easytrade.EasyTradeMod;
import com.pedromartz.easytrade.trading.TradeOfferGroup;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record LockedTradesPayload(int containerId, int requestId, List<TradeOfferGroup> groups) implements CustomPacketPayload {
	private static final StreamCodec<RegistryFriendlyByteBuf, List<TradeOfferGroup>> GROUPS_CODEC =
		ByteBufCodecs.<RegistryFriendlyByteBuf, TradeOfferGroup, ArrayList<TradeOfferGroup>>collection(
			ArrayList::new,
			TradeOfferGroup.STREAM_CODEC
		).map(List::copyOf, ArrayList::new);
	public static final Type<LockedTradesPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(EasyTradeMod.MOD_ID, "locked_trades"));
	public static final StreamCodec<RegistryFriendlyByteBuf, LockedTradesPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.CONTAINER_ID,
		LockedTradesPayload::containerId,
		ByteBufCodecs.VAR_INT,
		LockedTradesPayload::requestId,
		GROUPS_CODEC,
		LockedTradesPayload::groups,
		LockedTradesPayload::new
	);

	@Override
	public Type<LockedTradesPayload> type() {
		return TYPE;
	}
}
