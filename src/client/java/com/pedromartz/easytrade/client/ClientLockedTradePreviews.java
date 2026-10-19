package com.pedromartz.easytrade.client;

import com.pedromartz.easytrade.network.LockedTradesPayload;
import com.pedromartz.easytrade.network.RequestLockedTradesPayload;
import com.pedromartz.easytrade.trading.TradeOfferGroup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;

public final class ClientLockedTradePreviews {
	private static final Map<Integer, Preview> PREVIEWS = new HashMap<>();
	private static int lastInteractedVillagerId = -1;
	private static int nextRequestId;

	private ClientLockedTradePreviews() {
	}

	public static void rememberVillagerInteraction(final Entity entity) {
		lastInteractedVillagerId = entity instanceof Villager ? entity.getId() : -1;
	}

	public static void requestServerPreview(final MerchantMenu menu) {
		int containerId = menu.containerId;
		PREVIEWS.clear();
		if (!EasyTradeConfig.areLockedTradePreviewsEnabled()) {
			return;
		}
		int currentOfferCount = menu.getOffers().size();
		int requestId = ++nextRequestId;
		String villagerState = currentVillagerState();
		if (ClientPlayNetworking.canSend(RequestLockedTradesPayload.TYPE)) {
			PREVIEWS.put(containerId, new Preview(currentOfferCount, List.of(), true, requestId, villagerState));
			ClientPlayNetworking.send(new RequestLockedTradesPayload(containerId, requestId));
		} else {
			PREVIEWS.put(containerId, new Preview(currentOfferCount, List.of(), false, requestId, villagerState));
		}
	}

	public static Preview get(final int containerId) {
		if (!EasyTradeConfig.areLockedTradePreviewsEnabled()) {
			return null;
		}
		return PREVIEWS.get(containerId);
	}

	public static void clear() {
		PREVIEWS.clear();
	}

	public static void updateCurrentOfferCount(final int containerId, final int offerCount) {
		Preview current = PREVIEWS.get(containerId);
		if (current != null && current.currentOfferCount() != offerCount) {
			PREVIEWS.put(
				containerId,
				new Preview(offerCount, current.groups(), current.pending(), current.requestId(), current.villagerState())
			);
		}
	}

	public static void refreshIfOpen(final MerchantMenu menu) {
		if (!PREVIEWS.containsKey(menu.containerId)) {
			return;
		}
		if (Minecraft.getInstance().gui.screen() instanceof MerchantScreen screen && screen.getMenu() == menu) {
			requestServerPreview(menu);
		}
	}

	public static void refreshIfVillagerChanged(final MerchantMenu menu) {
		Preview current = PREVIEWS.get(menu.containerId);
		if (current != null && !Objects.equals(current.villagerState(), currentVillagerState())) {
			requestServerPreview(menu);
		}
	}

	public static void receive(final LockedTradesPayload payload) {
		Preview current = PREVIEWS.get(payload.containerId());
		if (current != null && current.requestId() == payload.requestId()) {
			PREVIEWS.put(
				payload.containerId(),
				new Preview(current.currentOfferCount(), payload.groups(), false, current.requestId(), current.villagerState())
			);
		}
	}

	private static String currentVillagerState() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || !(minecraft.level.getEntity(lastInteractedVillagerId) instanceof Villager villager)) {
			return "none:" + lastInteractedVillagerId;
		}
		String profession = villager.getVillagerData().profession().unwrapKey().map(key -> key.identifier().toString()).orElse("");
		return villager.getUUID() + ":" + profession + ":" + villager.getVillagerData().level();
	}

	public record Preview(int currentOfferCount, List<TradeOfferGroup> groups, boolean pending, int requestId, String villagerState) {
	}
}
