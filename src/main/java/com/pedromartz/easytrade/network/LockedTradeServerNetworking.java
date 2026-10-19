package com.pedromartz.easytrade.network;

import com.pedromartz.easytrade.mixin.MerchantMenuAccessor;
import com.pedromartz.easytrade.trading.LockedTradeOfferService;
import com.pedromartz.easytrade.trading.TradeOfferGroup;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;

public final class LockedTradeServerNetworking {
	private LockedTradeServerNetworking() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(RequestLockedTradesPayload.TYPE, (payload, context) ->
			context.server().execute(() -> sendLockedOffers(context.player(), payload))
		);
	}

	private static void sendLockedOffers(final ServerPlayer player, final RequestLockedTradesPayload payload) {
		if (!(player.containerMenu instanceof MerchantMenu menu) || menu.containerId != payload.containerId()) {
			return;
		}

		Merchant merchant = ((MerchantMenuAccessor)menu).easyTrade$getTrader();
		if (!(merchant instanceof Villager villager) || villager.getTradingPlayer() != player || !villager.isAlive()) {
			return;
		}

		List<TradeOfferGroup> groups =
			LockedTradeOfferService.getOrGenerate(villager, (ServerLevel)villager.level()).groups();
		if (ServerPlayNetworking.canSend(player, LockedTradesPayload.TYPE)) {
			ServerPlayNetworking.send(player, new LockedTradesPayload(menu.containerId, payload.requestId(), groups));
		}
	}
}
