package com.pedromartz.tradeassistant.mixin;

import com.pedromartz.tradeassistant.trading.LockedTradeCache;
import com.pedromartz.tradeassistant.trading.VillagerLockedTradeAccess;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerLockedTradesMixin implements VillagerLockedTradeAccess {
	@Unique
	private @Nullable LockedTradeCache tradeAssistant$lockedTradeCache;

	@Override
	public @Nullable LockedTradeCache tradeAssistant$getLockedTradeCache() {
		return this.tradeAssistant$lockedTradeCache;
	}

	@Override
	public void tradeAssistant$setLockedTradeCache(final LockedTradeCache cache) {
		this.tradeAssistant$lockedTradeCache = cache;
	}

	@Inject(method = "setVillagerData", at = @At("HEAD"))
	private void tradeAssistant$invalidateCacheOnProfessionChange(final VillagerData data, final CallbackInfo callbackInfo) {
		if (this.tradeAssistant$lockedTradeCache != null) {
			String newProfessionId = data.profession().unwrapKey().map(key -> key.identifier().toString()).orElse("");
			if (!this.tradeAssistant$lockedTradeCache.professionId().equals(newProfessionId)) {
				this.tradeAssistant$lockedTradeCache = null;
			}
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void tradeAssistant$saveLockedTrades(final ValueOutput output, final CallbackInfo callbackInfo) {
		if (this.tradeAssistant$lockedTradeCache != null) {
			output.store("TradeAssistantLockedTrades", LockedTradeCache.CODEC, this.tradeAssistant$lockedTradeCache);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void tradeAssistant$loadLockedTrades(final ValueInput input, final CallbackInfo callbackInfo) {
		this.tradeAssistant$lockedTradeCache = input.read("TradeAssistantLockedTrades", LockedTradeCache.CODEC).orElse(null);
	}
}
