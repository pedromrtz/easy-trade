package com.pedromartz.tradeassistant.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class EnchantmentLabelsToggleButton extends Button {
	private static final ItemStack BOOK_ICON = new ItemStack(Items.ENCHANTED_BOOK);
	private static final float ICON_SCALE = 0.75F;
	private boolean labelsVisible;

	public EnchantmentLabelsToggleButton(final int x, final int y, final boolean labelsVisible, final Button.OnPress onPress) {
		super(x, y, 16, 16, Component.empty(), onPress, DEFAULT_NARRATION);
		this.setLabelsVisible(labelsVisible);
	}

	public void setLabelsVisible(final boolean labelsVisible) {
		this.labelsVisible = labelsVisible;
		String tooltipKey = labelsVisible ? "trade-assistant.labels.tooltip.enabled" : "trade-assistant.labels.tooltip.disabled";
		this.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
	}

	@Override
	protected void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
		int x = this.getX();
		int y = this.getY();

		graphics.pose().pushMatrix();
		graphics.pose().translate(x + 2.0F, y + 2.0F);
		graphics.pose().scale(ICON_SCALE, ICON_SCALE);
		graphics.renderItem(BOOK_ICON, 0, 0);
		graphics.pose().popMatrix();

		graphics.fill(x + 10, y + 10, x + 16, y + 16, 0xFF171717);
		graphics.fill(x + 11, y + 11, x + 15, y + 15, this.labelsVisible ? 0xFF5DE05D : 0xFFE34B4B);
	}
}
