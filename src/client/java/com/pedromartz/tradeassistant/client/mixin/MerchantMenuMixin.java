package com.pedromartz.tradeassistant.client.mixin;

import com.pedromartz.tradeassistant.client.ClientLockedTradePreviews;
import com.pedromartz.tradeassistant.mixin.MerchantMenuAccessor;
import com.pedromartz.tradeassistant.trading.TradeOfferGroup;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {
	@Inject(method = "setOffers", at = @At("TAIL"))
	private void tradeAssistant$refreshLockedOffers(final MerchantOffers offers, final CallbackInfo callbackInfo) {
		ClientLockedTradePreviews.refreshIfOpen((MerchantMenu)(Object)this);
	}

	@Inject(method = "getOffers", at = @At("RETURN"), cancellable = true)
	private void tradeAssistant$appendLockedOffers(final CallbackInfoReturnable<MerchantOffers> callbackInfo) {
		MerchantMenu menu = (MerchantMenu)(Object)this;
		// In singleplayer this mixin also exists on the integrated server's menus.
		if (!((MerchantMenuAccessor)menu).tradeAssistant$getTrader().isClientSide()) {
			return;
		}
		MerchantOffers originalOffers = callbackInfo.getReturnValue();
		ClientLockedTradePreviews.updateCurrentOfferCount(menu.containerId, originalOffers.size());
		ClientLockedTradePreviews.Preview preview = ClientLockedTradePreviews.get(menu.containerId);
		if (preview == null || preview.groups().isEmpty()) {
			return;
		}

		MerchantOffers offers = originalOffers.copy();
		for (TradeOfferGroup group : preview.groups()) {
			offers.addAll(group.offers().copy());
		}
		callbackInfo.setReturnValue(offers);
	}
}
