package com.pedromartz.tradeassistant.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;

public final class EnchantmentTradeLabelRenderer {
	private EnchantmentTradeLabelRenderer() {
	}

	public static boolean areLabelsVisible() {
		return TradeAssistantConfig.areEnchantmentLabelsEnabled();
	}

	public static boolean toggleLabels() {
		boolean enabled = !TradeAssistantConfig.areEnchantmentLabelsEnabled();
		TradeAssistantConfig.setEnchantmentLabelsEnabled(enabled);
		return enabled;
	}

	public static void drawAll(final GuiGraphics graphics, final List<Label> labels) {
		Font font = Minecraft.getInstance().font;
		List<PreparedLabel> preparedLabels = new ArrayList<>(labels.size());
		for (Label label : labels) {
			ClientTooltipComponent text = ClientTooltipComponent.create(label.text().getVisualOrderText());
			int width = text.getWidth(font);
			int height = text.getHeight(font) - 2;
			int textX = label.anchorX() - width / 2;
			preparedLabels.add(new PreparedLabel(text, textX, label.textY(), width, height));
		}

		for (PreparedLabel label : preparedLabels) {
			TooltipRenderUtil.renderTooltipBackground(graphics, label.textX(), label.textY(), label.width(), label.height(), null);
		}
		for (PreparedLabel label : preparedLabels) {
			label.text().renderText(graphics, font, label.textX(), label.textY());
		}
	}

	public record Label(Component text, int anchorX, int textY) {
	}

	private record PreparedLabel(ClientTooltipComponent text, int textX, int textY, int width, int height) {
	}
}
