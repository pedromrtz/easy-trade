package com.pedromartz.tradeassistant.mixin;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractVillager.class)
public interface AbstractVillagerTradeAccessor {
	@Invoker("addOffersFromItemListings")
	void tradeAssistant$generateOffersFromItemListings(
		MerchantOffers offers, VillagerTrades.ItemListing[] listings, int numberOfOffers
	);
}
