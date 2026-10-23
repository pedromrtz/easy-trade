package com.pedromartz.tradeassistant.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.MerchantMenu;

public final class TradeCyclingButton extends Button.Plain {
	private final MerchantMenu menu;
	private String tooltipKey = "";

	public TradeCyclingButton(final int x, final int y, final MerchantMenu menu) {
		super(x, y, 18, 18, Component.literal("↻"), button -> ClientTradeCycling.request(menu), DEFAULT_NARRATION);
		this.menu = menu;
		this.updateState();
	}

	public void updateState() {
		this.visible = TradeAssistantConfig.isTradeCyclingEnabled() && ClientTradeCycling.isSupported();
		this.active = this.visible && ClientTradeCycling.canCycle(this.menu);
		String key = ClientTradeCycling.isWaiting(this.menu) ? "trade-assistant.cycling.waiting"
			: ClientTradeCycling.isEligible(this.menu) ? "trade-assistant.cycling.tooltip" : "trade-assistant.cycling.locked";
		if (!key.equals(this.tooltipKey)) {
			this.tooltipKey = key;
			MutableComponent tooltip = Component.translatable(key);
			if (key.equals("trade-assistant.cycling.tooltip")) {
				tooltip = tooltip.append(Component.literal(" ("))
					.append(ClientTradeCycling.cycleKey().getTranslatedKeyMessage())
					.append(Component.literal(")"));
			}
			this.setTooltip(Tooltip.create(tooltip));
		}
	}
}
