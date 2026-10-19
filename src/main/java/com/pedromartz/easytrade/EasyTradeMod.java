package com.pedromartz.easytrade;

import com.pedromartz.easytrade.network.CycleTradesPayload;
import com.pedromartz.easytrade.network.CycleTradesResultPayload;
import com.pedromartz.easytrade.network.LockedTradeServerNetworking;
import com.pedromartz.easytrade.network.LockedTradesPayload;
import com.pedromartz.easytrade.network.RequestLockedTradesPayload;
import com.pedromartz.easytrade.network.TradeCyclingServerNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyTradeMod implements ModInitializer {
	public static final String MOD_ID = "easy-trade";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Easy Trade initialized.");
		PayloadTypeRegistry.serverboundPlay().register(RequestLockedTradesPayload.TYPE, RequestLockedTradesPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(LockedTradesPayload.TYPE, LockedTradesPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(CycleTradesPayload.TYPE, CycleTradesPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(CycleTradesResultPayload.TYPE, CycleTradesResultPayload.STREAM_CODEC);
		LockedTradeServerNetworking.register();
		TradeCyclingServerNetworking.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
