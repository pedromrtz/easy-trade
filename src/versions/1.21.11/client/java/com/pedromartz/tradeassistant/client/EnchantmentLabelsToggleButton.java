package com.pedromartz.tradeassistant.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class EnchantmentLabelsToggleButton extends Button.Plain {
	public EnchantmentLabelsToggleButton(final int x, final int y, final boolean labelsVisible, final Button.OnPress onPress) {
		super(x, y, 16, 16, Component.literal("E"), onPress, DEFAULT_NARRATION);
		this.setLabelsVisible(labelsVisible);
	}

	public void setLabelsVisible(final boolean labelsVisible) {
		String tooltipKey = labelsVisible ? "trade-assistant.labels.tooltip.enabled" : "trade-assistant.labels.tooltip.disabled";
		this.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
	}
}
