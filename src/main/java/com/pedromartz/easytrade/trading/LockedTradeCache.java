package com.pedromartz.easytrade.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record LockedTradeCache(String professionId, List<TradeOfferGroup> groups) {
	public static final Codec<LockedTradeCache> CODEC = RecordCodecBuilder.create(
		i -> i.group(
				Codec.STRING.fieldOf("profession").forGetter(LockedTradeCache::professionId),
				TradeOfferGroup.CODEC.listOf().fieldOf("groups").forGetter(LockedTradeCache::groups)
			)
			.apply(i, LockedTradeCache::new)
	);
}
