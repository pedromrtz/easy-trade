package com.pedromartz.easytrade.trading;

import com.pedromartz.easytrade.mixin.AbstractVillagerTradeAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;

public final class LockedTradeOfferService {
	private LockedTradeOfferService() {
	}

	public static LockedTradeCache getOrGenerate(final Villager villager, final ServerLevel level) {
		VillagerLockedTradeAccess tradeAccess = (VillagerLockedTradeAccess)villager;
		String professionId = villager.getVillagerData().profession().unwrapKey().map(key -> key.identifier().toString()).orElse("");
		LockedTradeCache cache = tradeAccess.easyTrade$getLockedTradeCache();
		if (cache != null && cache.professionId().equals(professionId)) {
			return cache;
		}

		tradeAccess.easyTrade$setLockedTradeCache(null);
		VillagerProfession profession = villager.getVillagerData().profession().value();
		List<TradeOfferGroup> groups = new ArrayList<>();
		for (int tradeLevel = villager.getVillagerData().level() + 1; tradeLevel <= 5; tradeLevel++) {
			ResourceKey<TradeSet> tradeSetKey = profession.getTrades(tradeLevel);
			if (tradeSetKey == null) {
				continue;
			}

			MerchantOffers offers = new MerchantOffers();
			((AbstractVillagerTradeAccessor)villager).easyTrade$generateOffersFromTradeSet(level, offers, tradeSetKey);
			groups.add(new TradeOfferGroup(tradeLevel, offers));
		}

		cache = new LockedTradeCache(professionId, List.copyOf(groups));
		tradeAccess.easyTrade$setLockedTradeCache(cache);
		return cache;
	}
}
