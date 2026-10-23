package com.pedromartz.tradeassistant.mixin;

import com.pedromartz.tradeassistant.trading.LockedTradeCache;
import com.pedromartz.tradeassistant.trading.TradeOfferGroup;
import com.pedromartz.tradeassistant.trading.VillagerLockedTradeAccess;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerTradeMixin {
	@Inject(method = "addOffersFromItemListings", at = @At("HEAD"), cancellable = true)
	private void tradeAssistant$reusePreviewedOffers(
		final ServerLevel level,
		final MerchantOffers offers,
		final VillagerTrades.ItemListing[] listings,
		final int numberOfOffers,
		final CallbackInfo callbackInfo
	) {
		if (!((Object)this instanceof Villager villager)) {
			return;
		}

		VillagerLockedTradeAccess tradeAccess = (VillagerLockedTradeAccess)villager;
		LockedTradeCache cache = tradeAccess.tradeAssistant$getLockedTradeCache();
		if (cache == null) {
			return;
		}

		ResourceKey<VillagerProfession> professionKey = villager.getVillagerData().profession().unwrapKey().orElse(null);
		String professionId = professionKey == null ? "" : professionKey.identifier().toString();
		if (!cache.professionId().equals(professionId)) {
			tradeAccess.tradeAssistant$setLockedTradeCache(null);
			return;
		}

		int villagerLevel = villager.getVillagerData().level();
		Int2ObjectMap<VillagerTrades.ItemListing[]> tradeLevels = professionKey == null ? null : VillagerTrades.TRADES.get(professionKey);
		if (tradeLevels == null || listings != tradeLevels.get(villagerLevel)) {
			return;
		}

		TradeOfferGroup cachedGroup = cache.groups().stream().filter(group -> group.level() == villagerLevel).findFirst().orElse(null);
		if (cachedGroup == null) {
			return;
		}

		offers.addAll(cachedGroup.offers().copy());
		List<TradeOfferGroup> remainingGroups = cache.groups().stream().filter(group -> group.level() != villagerLevel).toList();
		tradeAccess.tradeAssistant$setLockedTradeCache(new LockedTradeCache(professionId, remainingGroups));
		callbackInfo.cancel();
	}
}
