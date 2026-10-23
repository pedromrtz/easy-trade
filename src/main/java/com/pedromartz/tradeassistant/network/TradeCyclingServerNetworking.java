package com.pedromartz.tradeassistant.network;

import com.pedromartz.tradeassistant.trading.TradeCyclingService;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class TradeCyclingServerNetworking {
	private TradeCyclingServerNetworking() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(CycleTradesPayload.TYPE, (payload, context) ->
			context.server().execute(() -> {
				// The server never requires clients to register or receive our optional payloads.
				if (!ServerPlayNetworking.canSend(context.player(), CycleTradesResultPayload.TYPE)) {
					return;
				}
				boolean cycled = TradeCyclingService.cycle(context.player(), payload.containerId());
				ServerPlayNetworking.send(context.player(), new CycleTradesResultPayload(payload.containerId(), payload.requestId(), cycled));
			})
		);
	}
}
