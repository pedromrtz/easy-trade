package com.pedromartz.easytrade.mixin;

import com.pedromartz.easytrade.trading.LockedTradeCache;
import com.pedromartz.easytrade.trading.VillagerLockedTradeAccess;
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
	private @Nullable LockedTradeCache easyTrade$lockedTradeCache;

	@Override
	public @Nullable LockedTradeCache easyTrade$getLockedTradeCache() {
		return this.easyTrade$lockedTradeCache;
	}

	@Override
	public void easyTrade$setLockedTradeCache(final LockedTradeCache cache) {
		this.easyTrade$lockedTradeCache = cache;
	}

	@Inject(method = "setVillagerData", at = @At("HEAD"))
	private void easyTrade$invalidateCacheOnProfessionChange(final VillagerData data, final CallbackInfo callbackInfo) {
		if (this.easyTrade$lockedTradeCache != null) {
			String newProfessionId = data.profession().unwrapKey().map(key -> key.identifier().toString()).orElse("");
			if (!this.easyTrade$lockedTradeCache.professionId().equals(newProfessionId)) {
				this.easyTrade$lockedTradeCache = null;
			}
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void easyTrade$saveLockedTrades(final ValueOutput output, final CallbackInfo callbackInfo) {
		if (this.easyTrade$lockedTradeCache != null) {
			output.store("EasyTradeLockedTrades", LockedTradeCache.CODEC, this.easyTrade$lockedTradeCache);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void easyTrade$loadLockedTrades(final ValueInput input, final CallbackInfo callbackInfo) {
		this.easyTrade$lockedTradeCache = input.read("EasyTradeLockedTrades", LockedTradeCache.CODEC).orElse(null);
	}
}
