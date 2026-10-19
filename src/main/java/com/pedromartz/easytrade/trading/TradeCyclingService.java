package com.pedromartz.easytrade.trading;

import com.pedromartz.easytrade.EasyTradeMod;
import com.pedromartz.easytrade.mixin.MerchantMenuAccessor;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;

public final class TradeCyclingService {
	// Server-thread only; weak keys release villagers when their chunks unload.
	private static final Map<Villager, Long> NEXT_CYCLE_TICK = new WeakHashMap<>();
	private static final int COOLDOWN_TICKS = 5;

	private TradeCyclingService() {
	}

	public static boolean cycle(final ServerPlayer player, final int containerId) {
		if (!player.isAlive() || player.isSpectator() || !(player.containerMenu instanceof MerchantMenu menu)
			|| menu.containerId != containerId) {
			return false;
		}

		if (!(((MerchantMenuAccessor)menu).easyTrade$getTrader() instanceof Villager villager)
			|| villager.isBaby() || !villager.stillValid(player)
			|| villager.getVillagerData().level() != 1 || villager.getVillagerXp() != 0) {
			return false;
		}

		MerchantOffers oldOffers = villager.getOffers();
		if (oldOffers.isEmpty() || oldOffers.stream().anyMatch(offer -> offer.getUses() > 0)) {
			return false;
		}

		long now = villager.level().getGameTime();
		if (now < NEXT_CYCLE_TICK.getOrDefault(villager, Long.MIN_VALUE)) {
			return false;
		}
		NEXT_CYCLE_TICK.put(villager, now + COOLDOWN_TICKS);

		VillagerLockedTradeAccess access = (VillagerLockedTradeAccess)villager;
		LockedTradeCache oldCache = access.easyTrade$getLockedTradeCache();
		access.easyTrade$setLockedTradeCache(null);
		MerchantOffers newOffers;
		try {
			// Use the normal lazy generation path, including modded updateTrades hooks and player discounts.
			villager.setOffers(null);
			newOffers = villager.getOffers();
			if (newOffers.isEmpty()) {
				villager.setOffers(oldOffers);
				access.easyTrade$setLockedTradeCache(oldCache);
			}
		} catch (RuntimeException exception) {
			villager.setOffers(oldOffers);
			access.easyTrade$setLockedTradeCache(oldCache);
			EasyTradeMod.LOGGER.warn("Could not cycle trades for villager {}", villager.getUUID(), exception);
			newOffers = oldOffers;
		}

		// Recompute the output from the existing inputs; never remove or grant payment items.
		menu.setSelectionHint(0);
		menu.updateSellItem();
		menu.broadcastChanges();
		player.sendMerchantOffers(
			containerId, villager.getOffers(), villager.getVillagerData().level(),
			villager.getVillagerXp(), villager.showProgressBar(), villager.canRestock()
		);
		return !newOffers.isEmpty() && newOffers != oldOffers;
	}
}
