package com.pedromartz.easytrade.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.trading.MerchantOffers;

public record TradeOfferGroup(int level, MerchantOffers offers) {
	public static final Codec<TradeOfferGroup> CODEC = RecordCodecBuilder.create(
		i -> i.group(
				Codec.INT.fieldOf("level").forGetter(TradeOfferGroup::level),
				MerchantOffers.CODEC.fieldOf("offers").forGetter(TradeOfferGroup::offers)
			)
			.apply(i, TradeOfferGroup::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, TradeOfferGroup> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		TradeOfferGroup::level,
		MerchantOffers.STREAM_CODEC,
		TradeOfferGroup::offers,
		TradeOfferGroup::new
	);
}
