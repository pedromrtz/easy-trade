package com.pedromartz.tradeassistant.network;

import com.pedromartz.tradeassistant.TradeAssistantMod;
import com.pedromartz.tradeassistant.trading.TradeOfferGroup;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LockedTradesPayload(int containerId, int requestId, List<TradeOfferGroup> groups) implements CustomPacketPayload {
	private static final StreamCodec<RegistryFriendlyByteBuf, List<TradeOfferGroup>> GROUPS_CODEC =
		ByteBufCodecs.<RegistryFriendlyByteBuf, TradeOfferGroup, ArrayList<TradeOfferGroup>>collection(
			ArrayList::new, TradeOfferGroup.STREAM_CODEC
		).map(List::copyOf, ArrayList::new);
	public static final Type<LockedTradesPayload> TYPE =
		new Type<>(ResourceLocation.fromNamespaceAndPath(TradeAssistantMod.MOD_ID, "locked_trades"));
	public static final StreamCodec<RegistryFriendlyByteBuf, LockedTradesPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, LockedTradesPayload::containerId,
		ByteBufCodecs.VAR_INT, LockedTradesPayload::requestId,
		GROUPS_CODEC, LockedTradesPayload::groups,
		LockedTradesPayload::new
	);

	@Override
	public Type<LockedTradesPayload> type() {
		return TYPE;
	}
}
