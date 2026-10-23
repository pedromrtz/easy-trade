package com.pedromartz.tradeassistant.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;
import com.pedromartz.tradeassistant.network.LockedTradesPayload;

public class TradeAssistantClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		TradeAssistantConfig.load();
		ClientTradeCycling.initialize();
		ClientPlayNetworking.registerGlobalReceiver(LockedTradesPayload.TYPE, (payload, context) ->
			context.client().execute(() -> ClientLockedTradePreviews.receive(payload))
		);

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClientSide()) {
				ClientLockedTradePreviews.rememberVillagerInteraction(entity);
			}
			return InteractionResult.PASS;
		});
	}
}
