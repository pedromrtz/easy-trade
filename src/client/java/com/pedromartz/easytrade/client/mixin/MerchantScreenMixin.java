package com.pedromartz.easytrade.client.mixin;

import java.util.ArrayList;
import java.util.List;

import com.pedromartz.easytrade.client.ClientLockedTradePreviews;
import com.pedromartz.easytrade.client.ClientTradeCycling;
import com.pedromartz.easytrade.client.EnchantmentLabelsToggleButton;
import com.pedromartz.easytrade.client.EnchantmentTradeLabelRenderer;
import com.pedromartz.easytrade.client.TradeCyclingButton;
import com.pedromartz.easytrade.client.TradeCyclingScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin implements TradeCyclingScreen {
	private static final int SCREEN_LEFT_OFFSET = 138;
	private static final int SCREEN_TOP_OFFSET = 83;
	private static final int FIRST_OFFER_X = 73;
	private static final int FIRST_OFFER_Y = 19;
	private static final int OFFER_ROW_HEIGHT = 20;
	private static final int VISIBLE_OFFER_COUNT = 7;

	@Shadow
	private int scrollOff;
	@Shadow
	private int shopItem;
	@Unique
	private int easyTrade$previewContainerId = -1;
	@Unique
	private TradeCyclingButton easyTrade$cycleButton;

	@Override
	public void easyTrade$resetTradeSelection() {
		this.shopItem = 0;
		this.scrollOff = 0;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void easyTrade$addLabelsToggleButton(final CallbackInfo callbackInfo) {
		MerchantScreen screen = (MerchantScreen)(Object)this;
		int left = screen.width / 2 - SCREEN_LEFT_OFFSET;
		int top = screen.height / 2 - SCREEN_TOP_OFFSET;
		boolean labelsVisible = EnchantmentTradeLabelRenderer.areLabelsVisible();

		EnchantmentLabelsToggleButton toggleButton = new EnchantmentLabelsToggleButton(
			left + 4,
			top + 1,
			labelsVisible,
			button -> {
				boolean nowVisible = EnchantmentTradeLabelRenderer.toggleLabels();
				((EnchantmentLabelsToggleButton)button).setLabelsVisible(nowVisible);
			}
		);
		((ScreenAccessor)(Object)this).easyTrade$invokeAddRenderableWidget(toggleButton);
		this.easyTrade$cycleButton = new TradeCyclingButton(left + 105, top + 5, screen.getMenu());
		((ScreenAccessor)(Object)this).easyTrade$invokeAddRenderableWidget(this.easyTrade$cycleButton);

		if (this.easyTrade$previewContainerId != screen.getMenu().containerId) {
			ClientTradeCycling.clearPendingRequest();
			this.easyTrade$previewContainerId = screen.getMenu().containerId;
			ClientLockedTradePreviews.requestServerPreview(screen.getMenu());
		}
	}

	@Inject(method = "extractContents", at = @At("HEAD"))
	private void easyTrade$updateCyclingControls(
		final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float delta, final CallbackInfo callbackInfo
	) {
		MerchantScreen screen = (MerchantScreen)(Object)this;
		MerchantOffers offers = screen.getMenu().getOffers();
		// Cycling replaces the list, which can shrink while the user is scrolled to locked rows.
		this.shopItem = Math.max(0, Math.min(this.shopItem, offers.size() - 1));
		this.scrollOff = Math.max(0, Math.min(this.scrollOff, offers.size() - VISIBLE_OFFER_COUNT));
		if (this.easyTrade$cycleButton != null) {
			this.easyTrade$cycleButton.updateState();
		}
	}

	@Inject(method = "postButtonClick", at = @At("HEAD"), cancellable = true)
	private void easyTrade$preventLockedTradeSelection(final CallbackInfo callbackInfo) {
		MerchantScreen screen = (MerchantScreen)(Object)this;
		ClientLockedTradePreviews.Preview preview = ClientLockedTradePreviews.get(screen.getMenu().containerId);
		if (preview != null && this.shopItem >= preview.currentOfferCount()) {
			this.shopItem = Math.max(0, preview.currentOfferCount() - 1);
			callbackInfo.cancel();
		}
	}

	@Inject(method = "extractContents", at = @At("TAIL"))
	private void easyTrade$renderEnchantmentLabels(
		final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float delta, final CallbackInfo callbackInfo
	) {
		MerchantScreen screen = (MerchantScreen)(Object)this;
		MerchantMenu menu = screen.getMenu();
		ClientLockedTradePreviews.refreshIfVillagerChanged(menu);
		MerchantOffers offers = menu.getOffers();
		int left = screen.width / 2 - SCREEN_LEFT_OFFSET;
		int top = screen.height / 2 - SCREEN_TOP_OFFSET;

		int visibleEnd = Math.min(offers.size(), this.scrollOff + VISIBLE_OFFER_COUNT);
		ClientLockedTradePreviews.Preview preview = ClientLockedTradePreviews.get(menu.containerId);
		if (preview != null) {
			int firstLockedOffer = Math.max(this.scrollOff, preview.currentOfferCount());
			for (int offerIndex = firstLockedOffer; offerIndex < visibleEnd; offerIndex++) {
				int row = offerIndex - this.scrollOff;
				int rowX = left + 5;
				int rowY = top + 18 + row * OFFER_ROW_HEIGHT;
				graphics.fill(rowX, rowY, left + 93, rowY + OFFER_ROW_HEIGHT, 0x66000000);
			}
		}

		if (!EnchantmentTradeLabelRenderer.areLabelsVisible()) {
			return;
		}

		List<EnchantmentTradeLabelRenderer.Label> labels = new ArrayList<>();
		for (int offerIndex = this.scrollOff; offerIndex < visibleEnd; offerIndex++) {
			ItemStack result = offers.get(offerIndex).getResult();
			if (!result.is(Items.ENCHANTED_BOOK)) {
				continue;
			}

			ItemEnchantments enchantments = result.get(DataComponents.STORED_ENCHANTMENTS);
			if (enchantments == null || enchantments.isEmpty()) {
				continue;
			}

			MutableComponent label = Component.empty();
			for (var entry : enchantments.entrySet()) {
				if (!label.getString().isEmpty()) {
					label.append(Component.literal(" / "));
				}
				label.append(Enchantment.getFullname(entry.getKey(), entry.getIntValue()));
			}

			int row = offerIndex - this.scrollOff;
			int itemX = left + FIRST_OFFER_X;
			int itemY = top + FIRST_OFFER_Y + row * OFFER_ROW_HEIGHT;
			int textY = itemY + 14;
			labels.add(new EnchantmentTradeLabelRenderer.Label(label, itemX + 8, textY));
		}

		EnchantmentTradeLabelRenderer.drawAll(graphics, labels);
	}
}
