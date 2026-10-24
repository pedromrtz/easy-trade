package com.pedromartz.tradeassistant.trading;

import com.pedromartz.tradeassistant.mixin.AbstractVillagerTradeAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;

public final class LockedTradeOfferService {
	private LockedTradeOfferService() { }

	public static LockedTradeCache getOrGenerate(final Villager villager, final ServerLevel level) {
		VillagerLockedTradeAccess tradeAccess = (VillagerLockedTradeAccess)villager;
		ResourceKey<VillagerProfession> professionKey = villager.getVillagerData().profession().unwrapKey().orElse(null);
		String professionId = professionKey == null ? "" : professionKey.location().toString();
		LockedTradeCache cache = tradeAccess.tradeAssistant$getLockedTradeCache();
		if (cache != null && cache.professionId().equals(professionId)) {
			return cache;
		}

		tradeAccess.tradeAssistant$setLockedTradeCache(null);
		Int2ObjectMap<VillagerTrades.ItemListing[]> tradeLevels = professionKey == null ? null : VillagerTrades.TRADES.get(professionKey);
		List<TradeOfferGroup> groups = new ArrayList<>();
		if (tradeLevels != null) {
			for (int tradeLevel = villager.getVillagerData().level() + 1; tradeLevel <= 5; tradeLevel++) {
				VillagerTrades.ItemListing[] listings = tradeLevels.get(tradeLevel);
				if (listings == null || listings.length == 0) {
					continue;
				}

				MerchantOffers offers = new MerchantOffers();
				((AbstractVillagerTradeAccessor)villager).tradeAssistant$generateOffersFromItemListings(offers, listings, 2);
				groups.add(new TradeOfferGroup(tradeLevel, offers));
			}
		}

		cache = new LockedTradeCache(professionId, List.copyOf(groups));
		tradeAccess.tradeAssistant$setLockedTradeCache(cache);
		return cache;
	}
}
