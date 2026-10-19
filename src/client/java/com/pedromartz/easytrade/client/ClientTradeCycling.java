package com.pedromartz.easytrade.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.pedromartz.easytrade.EasyTradeMod;
import com.pedromartz.easytrade.network.CycleTradesPayload;
import com.pedromartz.easytrade.network.CycleTradesResultPayload;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.inventory.MerchantMenu;

public final class ClientTradeCycling {
	private static KeyMapping cycleKey;
	private static int nextRequestId;
	private static int pendingRequestId = -1;
	private static int pendingContainerId = -1;
	private static long nextRequestTime;
	private static long requestDeadline;

	private ClientTradeCycling() {
	}

	public static void initialize() {
		cycleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.easy-trade.cycle_trades", InputConstants.KEY_C, KeyMapping.Category.register(EasyTradeMod.id("controls"))
		));
		ClientPlayNetworking.registerGlobalReceiver(CycleTradesResultPayload.TYPE, (payload, context) ->
			context.client().execute(() -> receive(payload))
		);
	}

	public static KeyMapping cycleKey() {
		return cycleKey;
	}

	public static boolean isSupported() {
		return Minecraft.getInstance().getConnection() != null && ClientPlayNetworking.canSend(CycleTradesPayload.TYPE);
	}

	public static boolean isEligible(final MerchantMenu menu) {
		return menu.showProgressBar() && menu.getTraderLevel() == 1 && menu.getTraderXp() == 0
			&& !menu.getOffers().isEmpty() && menu.getOffers().stream().noneMatch(offer -> offer.getUses() > 0);
	}

	public static boolean isWaiting(final MerchantMenu menu) {
		return pendingContainerId == menu.containerId && pendingRequestId != -1 && System.nanoTime() < requestDeadline;
	}

	public static boolean canCycle(final MerchantMenu menu) {
		return isSupported() && isEligible(menu) && !isWaiting(menu) && System.nanoTime() >= nextRequestTime;
	}

	public static boolean request(final MerchantMenu menu) {
		if (!canCycle(menu) || Minecraft.getInstance().player == null || Minecraft.getInstance().player.containerMenu != menu) {
			return false;
		}
		pendingRequestId = ++nextRequestId;
		pendingContainerId = menu.containerId;
		long now = System.nanoTime();
		nextRequestTime = now + 300_000_000L;
		requestDeadline = now + 3_000_000_000L;
		ClientPlayNetworking.send(new CycleTradesPayload(menu.containerId, pendingRequestId));
		return true;
	}

	private static void receive(final CycleTradesResultPayload payload) {
		if (payload.containerId() != pendingContainerId || payload.requestId() != pendingRequestId) {
			return;
		}
		pendingRequestId = -1;
		Minecraft minecraft = Minecraft.getInstance();
		if (payload.cycled() && minecraft.gui.screen() instanceof MerchantScreen screen
			&& screen.getMenu().containerId == payload.containerId()) {
			// Offer packets arrive before this acknowledgement; the menu hook refreshes locked offers.
			((TradeCyclingScreen)screen).easyTrade$resetTradeSelection();
			screen.getMenu().setSelectionHint(0);
			screen.getMenu().updateSellItem();
		}
	}

	public static void clearPendingRequest() {
		pendingRequestId = -1;
		pendingContainerId = -1;
	}
}
