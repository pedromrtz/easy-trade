package com.pedromartz.tradeassistant.mixin;

import com.pedromartz.tradeassistant.trading.LockedTradeCache;
import com.pedromartz.tradeassistant.trading.TradeOfferGroup;
import com.pedromartz.tradeassistant.trading.VillagerLockedTradeAccess;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerTradeMixin {
	@Inject(method = "addOffersFromTradeSet", at = @At("HEAD"), cancellable = true)
	private void tradeAssistant$reusePreviewedOffers(
		final ServerLevel level, final MerchantOffers offers, final ResourceKey<TradeSet> tradeSetKey, final CallbackInfo callbackInfo
	) {
		Object target = this;
		if (!(target instanceof Villager villager)) {
			return;
		}

		VillagerLockedTradeAccess tradeAccess = (VillagerLockedTradeAccess)villager;
		LockedTradeCache cache = tradeAccess.tradeAssistant$getLockedTradeCache();
		if (cache == null) {
			return;
		}

		String professionId = villager.getVillagerData().profession().unwrapKey().map(key -> key.identifier().toString()).orElse("");
		if (!cache.professionId().equals(professionId)) {
			tradeAccess.tradeAssistant$setLockedTradeCache(null);
			return;
		}

		int villagerLevel = villager.getVillagerData().level();
		ResourceKey<TradeSet> currentTradeSet = villager.getVillagerData().profession().value().getTrades(villagerLevel);
		if (!tradeSetKey.equals(currentTradeSet)) {
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
