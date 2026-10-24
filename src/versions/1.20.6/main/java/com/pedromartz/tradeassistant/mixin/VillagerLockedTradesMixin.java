package com.pedromartz.tradeassistant.mixin;

import com.pedromartz.tradeassistant.trading.LockedTradeCache;
import com.pedromartz.tradeassistant.trading.VillagerLockedTradeAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerLockedTradesMixin implements VillagerLockedTradeAccess {
	@Unique
	private LockedTradeCache tradeAssistant$lockedTradeCache;

	@Override
	public LockedTradeCache tradeAssistant$getLockedTradeCache() {
		return this.tradeAssistant$lockedTradeCache;
	}

	@Override
	public void tradeAssistant$setLockedTradeCache(final LockedTradeCache cache) {
		this.tradeAssistant$lockedTradeCache = cache;
	}

	@Inject(method = "setVillagerData", at = @At("HEAD"))
	private void tradeAssistant$invalidateCacheOnProfessionChange(final VillagerData data, final CallbackInfo callbackInfo) {
		if (this.tradeAssistant$lockedTradeCache != null) {
			String newProfessionId = BuiltInRegistries.VILLAGER_PROFESSION.getKey(data.getProfession()).toString();
			if (!this.tradeAssistant$lockedTradeCache.professionId().equals(newProfessionId)) {
				this.tradeAssistant$lockedTradeCache = null;
			}
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void tradeAssistant$saveLockedTrades(final CompoundTag output, final CallbackInfo callbackInfo) {
		if (this.tradeAssistant$lockedTradeCache != null) {
			output.put("TradeAssistantLockedTrades", LockedTradeCache.CODEC.encodeStart(NbtOps.INSTANCE, this.tradeAssistant$lockedTradeCache).getOrThrow());
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void tradeAssistant$loadLockedTrades(final CompoundTag input, final CallbackInfo callbackInfo) {
		this.tradeAssistant$lockedTradeCache = input.contains("TradeAssistantLockedTrades")
			? LockedTradeCache.CODEC.parse(NbtOps.INSTANCE, input.get("TradeAssistantLockedTrades")).result().orElse(null)
			: null;
	}
}
