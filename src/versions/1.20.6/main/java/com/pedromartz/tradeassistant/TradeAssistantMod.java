package com.pedromartz.tradeassistant;

import com.pedromartz.tradeassistant.network.LockedTradeServerNetworking;
import com.pedromartz.tradeassistant.network.TargetPayloadRegistration;
import com.pedromartz.tradeassistant.network.TradeCyclingServerNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeAssistantMod implements ModInitializer {
	public static final String MOD_ID = "trade-assistant";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Trade Assistant initialized.");
		TargetPayloadRegistration.register();
		LockedTradeServerNetworking.register();
		TradeCyclingServerNetworking.register();
	}

	public static ResourceLocation id(final String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
