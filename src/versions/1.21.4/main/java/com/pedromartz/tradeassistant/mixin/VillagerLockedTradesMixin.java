package com.pedromartz.tradeassistant.mixin;

import com.pedromartz.tradeassistant.trading.LockedTradeCache;
import com.pedromartz.tradeassistant.trading.VillagerLockedTradeAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
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
			RegistryAccess registries = ((Villager)(Object)this).registryAccess();
			output.put("TradeAssistantLockedTrades", LockedTradeCache.CODEC.encodeStart(
				RegistryOps.create(NbtOps.INSTANCE, registries), this.tradeAssistant$lockedTradeCache
			).getOrThrow());
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void tradeAssistant$loadLockedTrades(final CompoundTag input, final CallbackInfo callbackInfo) {
		if (input.contains("TradeAssistantLockedTrades")) {
			RegistryAccess registries = ((Villager)(Object)this).registryAccess();
			this.tradeAssistant$lockedTradeCache = LockedTradeCache.CODEC.parse(
				RegistryOps.create(NbtOps.INSTANCE, registries), input.get("TradeAssistantLockedTrades")
			).result().orElse(null);
		} else {
			this.tradeAssistant$lockedTradeCache = null;
		}
	}
}
