package com.pedromartz.tradeassistant.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pedromartz.tradeassistant.TradeAssistantMod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class TradeAssistantConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("trade-assistant.json");

	private static boolean enchantmentLabelsEnabled = true;
	private static boolean lockedTradePreviewsEnabled = true;
	private static boolean tradeCyclingEnabled = true;

	private TradeAssistantConfig() {
	}

	public static void load() {
		if (!Files.exists(CONFIG_FILE)) {
			save();
			return;
		}

		try (var reader = Files.newBufferedReader(CONFIG_FILE)) {
			ConfigData data = GSON.fromJson(reader, ConfigData.class);
			if (data == null) {
				return;
			}
			enchantmentLabelsEnabled = data.enchantmentLabelsEnabled;
			lockedTradePreviewsEnabled = data.lockedTradePreviewsEnabled;
			tradeCyclingEnabled = data.tradeCyclingEnabled;
		} catch (IOException | RuntimeException exception) {
			TradeAssistantMod.LOGGER.warn("Could not load Trade Assistant client config; using defaults", exception);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_FILE.getParent());
			try (var writer = Files.newBufferedWriter(CONFIG_FILE)) {
				GSON.toJson(new ConfigData(enchantmentLabelsEnabled, lockedTradePreviewsEnabled, tradeCyclingEnabled), writer);
			}
		} catch (IOException exception) {
			TradeAssistantMod.LOGGER.warn("Could not save Trade Assistant client config", exception);
		}
	}

	public static boolean areEnchantmentLabelsEnabled() {
		return enchantmentLabelsEnabled;
	}

	public static void setEnchantmentLabelsEnabled(final boolean enabled) {
		enchantmentLabelsEnabled = enabled;
		save();
	}

	public static boolean areLockedTradePreviewsEnabled() {
		return lockedTradePreviewsEnabled;
	}

	public static void setLockedTradePreviewsEnabled(final boolean enabled) {
		lockedTradePreviewsEnabled = enabled;
		if (!enabled) {
			ClientLockedTradePreviews.clear();
		}
		save();
	}

	public static boolean isTradeCyclingEnabled() {
		return tradeCyclingEnabled;
	}

	public static void setTradeCyclingEnabled(final boolean enabled) {
		tradeCyclingEnabled = enabled;
		save();
	}

	public static void apply(final boolean labelsEnabled, final boolean previewsEnabled, final boolean cyclingEnabled) {
		enchantmentLabelsEnabled = labelsEnabled;
		lockedTradePreviewsEnabled = previewsEnabled;
		tradeCyclingEnabled = cyclingEnabled;
		if (!previewsEnabled) {
			ClientLockedTradePreviews.clear();
		}
		save();
	}

	private record ConfigData(boolean enchantmentLabelsEnabled, boolean lockedTradePreviewsEnabled, boolean tradeCyclingEnabled) {
	}
}
