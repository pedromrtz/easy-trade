package com.pedromartz.tradeassistant.mixin;

import com.pedromartz.tradeassistant.trading.LockedTradeCache;
import com.pedromartz.tradeassistant.trading.TradeOfferGroup;
import com.pedromartz.tradeassistant.trading.VillagerLockedTradeAccess;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerTradeMixin {
	@Inject(method = "addOffersFromItemListings", at = @At("HEAD"), cancellable = true)
	private void tradeAssistant$reusePreviewedOffers(
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

		VillagerProfession profession = villager.getVillagerData().getProfession();
		String professionId = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).toString();
		if (!cache.professionId().equals(professionId)) {
			tradeAccess.tradeAssistant$setLockedTradeCache(null);
			return;
		}

		int villagerLevel = villager.getVillagerData().getLevel();
		Int2ObjectMap<VillagerTrades.ItemListing[]> tradeLevels = VillagerTrades.TRADES.get(profession);
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
