package com.pedromartz.tradeassistant.client;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class TradeAssistantConfigScreen extends Screen {
	private static final int SIDE_MARGIN = 32;
	private static final int ROW_HEIGHT = 28;

	private final Screen parent;
	private boolean labelsEnabled;
	private boolean previewsEnabled;
	private boolean cyclingEnabled;

	public TradeAssistantConfigScreen(final Screen parent) {
		super(Component.translatable("trade-assistant.config.title"));
		this.parent = parent;
		this.labelsEnabled = TradeAssistantConfig.areEnchantmentLabelsEnabled();
		this.previewsEnabled = TradeAssistantConfig.areLockedTradePreviewsEnabled();
		this.cyclingEnabled = TradeAssistantConfig.isTradeCyclingEnabled();
	}

	@Override
	protected void init() {
		int toggleWidth = Math.min(220, this.width / 3);
		int toggleX = this.width - SIDE_MARGIN - toggleWidth;
		int firstRowY = this.height / 2 - ROW_HEIGHT * 2;
		int labelWidth = toggleX - SIDE_MARGIN * 2;
		Component labelsText = Component.translatable("trade-assistant.config.labels");
		Component previewsText = Component.translatable("trade-assistant.config.previews");
		Component cyclingText = Component.translatable("trade-assistant.config.cycling");
		Component titleText = this.title;
		this.addRenderableWidget(new StringWidget((this.width - this.font.width(titleText)) / 2, firstRowY - 42,
			this.font.width(titleText), 20, titleText, this.font));
		this.addRenderableWidget(new StringWidget(SIDE_MARGIN, firstRowY, labelWidth, 20, labelsText, this.font));
		this.addRenderableWidget(new StringWidget(SIDE_MARGIN, firstRowY + ROW_HEIGHT, labelWidth, 20, previewsText, this.font));
		this.addRenderableWidget(new StringWidget(SIDE_MARGIN, firstRowY + ROW_HEIGHT * 2, labelWidth, 20, cyclingText, this.font));

		this.addRenderableWidget(this.createToggle(toggleX, firstRowY, toggleWidth, this.labelsEnabled,
			() -> this.labelsEnabled, enabled -> this.labelsEnabled = enabled));
		this.addRenderableWidget(this.createToggle(toggleX, firstRowY + ROW_HEIGHT, toggleWidth, this.previewsEnabled,
			() -> this.previewsEnabled, enabled -> this.previewsEnabled = enabled));
		this.addRenderableWidget(this.createToggle(toggleX, firstRowY + ROW_HEIGHT * 2, toggleWidth, this.cyclingEnabled,
			() -> this.cyclingEnabled, enabled -> this.cyclingEnabled = enabled));

		int footerY = this.height - 36;
		int footerGap = 12;
		int footerWidth = Math.min(220, (this.width - SIDE_MARGIN * 2 - footerGap) / 2);
		int footerLeft = (this.width - footerWidth * 2 - footerGap) / 2;
		this.addRenderableWidget(Button.builder(Component.translatable("trade-assistant.config.cancel"), button -> this.onClose())
			.bounds(footerLeft, footerY, footerWidth, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("trade-assistant.config.save"), button -> this.saveAndClose())
			.bounds(footerLeft + footerWidth + footerGap, footerY, footerWidth, 20).build());
	}

	private Button createToggle(
		final int x, final int y, final int width, final boolean initialValue,
		final BooleanSupplier currentValue, final Consumer<Boolean> updateValue
	) {
		Button[] buttonRef = new Button[1];
		buttonRef[0] = Button.builder(this.toggleMessage(initialValue), button -> {
			boolean enabled = !currentValue.getAsBoolean();
			updateValue.accept(enabled);
			buttonRef[0].setMessage(this.toggleMessage(enabled));
		}).bounds(x, y, width, 20).build();
		return buttonRef[0];
	}

	private Component toggleMessage(final boolean enabled) {
		return Component.translatable(enabled ? "trade-assistant.config.enabled" : "trade-assistant.config.disabled");
	}

	private void saveAndClose() {
		TradeAssistantConfig.apply(this.labelsEnabled, this.previewsEnabled, this.cyclingEnabled);
		this.minecraft.setScreen(this.parent);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}
}
