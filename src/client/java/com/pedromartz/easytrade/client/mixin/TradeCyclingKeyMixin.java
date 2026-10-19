package com.pedromartz.easytrade.client.mixin;

import com.pedromartz.easytrade.client.ClientTradeCycling;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class TradeCyclingKeyMixin {
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void easyTrade$cycleWithKey(final KeyEvent event, final CallbackInfoReturnable<Boolean> callbackInfo) {
		if ((Object)this instanceof MerchantScreen screen && !(screen.getFocused() instanceof EditBox)
			&& ClientTradeCycling.cycleKey().matches(event) && ClientTradeCycling.request(screen.getMenu())) {
			callbackInfo.setReturnValue(true);
		}
	}
}
