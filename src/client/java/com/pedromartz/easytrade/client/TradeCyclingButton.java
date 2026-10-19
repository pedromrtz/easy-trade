package com.pedromartz.easytrade.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.MerchantMenu;

public final class TradeCyclingButton extends Button.Plain {
	// Exact pixel contours prevent the two arrowheads from merging when outlined.
	private static final String[] TOP_ARROW = {
		".........#....",
		".........#H#..",
		"..#######HHH#.",
		".#HHHHHHHHHHH#",
		"#HSSSSSSSSSS#.",
		"#HS#######S#..",
		"####.....#...."
	};
	private final MerchantMenu menu;
	private String tooltipKey = "";

	public TradeCyclingButton(final int x, final int y, final MerchantMenu menu) {
		super(x, y, 18, 18, Component.translatable("key.easy-trade.cycle_trades"), button -> ClientTradeCycling.request(menu), DEFAULT_NARRATION);
		this.menu = menu;
		this.updateState();
	}

	public void updateState() {
		this.visible = ClientTradeCycling.isSupported();
		this.active = this.visible && ClientTradeCycling.canCycle(this.menu);
		String key = ClientTradeCycling.isWaiting(this.menu) ? "easy-trade.cycling.waiting"
			: ClientTradeCycling.isEligible(this.menu) ? "easy-trade.cycling.tooltip" : "easy-trade.cycling.locked";
		if (!key.equals(this.tooltipKey)) {
			this.tooltipKey = key;
			MutableComponent tooltip = Component.translatable(key);
			if (key.equals("easy-trade.cycling.tooltip")) {
				tooltip = tooltip.append(Component.literal(" ("))
					.append(ClientTradeCycling.cycleKey().getTranslatedKeyMessage())
					.append(Component.literal(")"));
			}
			this.setTooltip(Tooltip.create(tooltip));
		}
	}

	@Override
	protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float delta) {
		int x = this.getX();
		int y = this.getY();
		boolean highlighted = this.active && this.isHoveredOrFocused();
		int outline = this.active ? 0xFF414141 : 0xFF5F5F5F;
		int face = !this.active ? 0xFFA0A0A0 : highlighted ? 0xFFE2E2DC : 0xFFC9C9C2;
		int highlight = !this.active ? 0xFFC8C8C8 : highlighted ? 0xFFFFFFFF : 0xFFF1F1EB;
		int shadow = !this.active ? 0xFF898989 : 0xFFA0A09A;

		for (int row = 0; row < TOP_ARROW.length; row++) {
			String pixels = TOP_ARROW[row];
			for (int column = 0; column < pixels.length(); column++) {
				char pixel = pixels.charAt(column);
				int topColor = switch (pixel) {
					case '#' -> outline;
					case 'H' -> highlight;
					case 'S' -> shadow;
					default -> 0;
				};
				int bottomColor = switch (pixel) {
					case '#' -> outline;
					case 'H' -> shadow;
					case 'S' -> face;
					default -> 0;
				};
				if (topColor == 0) {
					continue;
				}
				int topX = x + 2 + column;
				int topY = y + 1 + row;
				int bottomX = x + 2 + pixels.length() - 1 - column;
				int bottomY = y + 16 - row;
				graphics.fill(topX, topY, topX + 1, topY + 1, topColor);
				graphics.fill(bottomX, bottomY, bottomX + 1, bottomY + 1, bottomColor);
			}
		}
	}
}
