package com.pedromartz.tradeassistant.client.mixin;

import com.pedromartz.tradeassistant.client.ClientTradeCycling;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class TradeCyclingKeyMixin {
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void tradeAssistant$cycleWithKey(final int keyCode, final int scanCode, final int modifiers, final CallbackInfoReturnable<Boolean> callbackInfo) {
		if ((Object)this instanceof MerchantScreen screen && !(screen.getFocused() instanceof EditBox)
			&& ClientTradeCycling.cycleKey().matches(keyCode, scanCode) && ClientTradeCycling.request(screen.getMenu())) {
			callbackInfo.setReturnValue(true);
		}
	}
}
