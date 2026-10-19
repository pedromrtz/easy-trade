package com.pedromartz.easytrade.client.mixin;

import java.util.ArrayList;
import java.util.List;

import com.pedromartz.easytrade.client.EnchantmentLabelsToggleButton;
import com.pedromartz.easytrade.client.EnchantmentTradeLabelRenderer;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin {
	private static final int SCREEN_LEFT_OFFSET = 138;
	private static final int SCREEN_TOP_OFFSET = 83;
	private static final int FIRST_OFFER_X = 73;
	private static final int FIRST_OFFER_Y = 19;
	private static final int OFFER_ROW_HEIGHT = 20;
	private static final int VISIBLE_OFFER_COUNT = 7;

	@Shadow
	private int scrollOff;

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
	}

	@Inject(method = "extractContents", at = @At("TAIL"))
	private void easyTrade$renderEnchantmentLabels(
		final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float delta, final CallbackInfo callbackInfo
	) {
		if (!EnchantmentTradeLabelRenderer.areLabelsVisible()) {
			return;
		}

		MerchantScreen screen = (MerchantScreen)(Object)this;
		MerchantMenu menu = screen.getMenu();
		MerchantOffers offers = menu.getOffers();
		int left = screen.width / 2 - SCREEN_LEFT_OFFSET;
		int top = screen.height / 2 - SCREEN_TOP_OFFSET;

		int visibleEnd = Math.min(offers.size(), this.scrollOff + VISIBLE_OFFER_COUNT);
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
