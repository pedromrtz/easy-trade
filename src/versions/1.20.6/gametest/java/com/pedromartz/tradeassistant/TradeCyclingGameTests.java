package com.pedromartz.tradeassistant;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import com.pedromartz.tradeassistant.trading.LockedTradeCache;
import com.pedromartz.tradeassistant.trading.LockedTradeOfferService;
import com.pedromartz.tradeassistant.trading.TradeCyclingService;
import com.pedromartz.tradeassistant.trading.VillagerLockedTradeAccess;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffers;

public class TradeCyclingGameTests implements FabricGameTest {
	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void cycleRefreshesOffersAndPreviewsWithoutConsumingInputs(final GameTestHelper helper) {
		Fixture fixture = createFixture(helper);
		Villager villager = fixture.villager();
		MerchantOffers oldOffers = villager.getOffers();
		LockedTradeCache oldCache = LockedTradeOfferService.getOrGenerate(villager, helper.getLevel());
		fixture.menu().getSlot(0).set(new ItemStack(Items.EMERALD, 64));
		fixture.menu().getSlot(1).set(new ItemStack(Items.BOOK, 12));

		check(TradeCyclingService.cycle(fixture.player(), fixture.menu().containerId), "An untraded novice should cycle");
		check(villager.getOffers() != oldOffers && !villager.getOffers().isEmpty(), "Offers must be regenerated");
		check(((VillagerLockedTradeAccess)villager).tradeAssistant$getLockedTradeCache() == null, "Old previews must be discarded");
		check(fixture.menu().getSlot(0).getItem().getCount() == 64, "Cycling must not consume emeralds");
		check(fixture.menu().getSlot(1).getItem().getCount() == 12, "Cycling must not consume books");
		LockedTradeCache newCache = LockedTradeOfferService.getOrGenerate(villager, helper.getLevel());
		check(newCache != oldCache && !newCache.groups().isEmpty(), "New previews must be generated for the new offers");
		check(fixture.player().lastSentOffers == villager.getOffers(), "Send real offers only, not locked rows");

		MerchantOffers afterFirstCycle = villager.getOffers();
		check(!TradeCyclingService.cycle(fixture.player(), fixture.menu().containerId), "Repeated requests in the same tick must be throttled");
		check(villager.getOffers() == afterFirstCycle, "Throttled requests must not change trades");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void rejectsUsedTradesEvenWhenXpIsZero(final GameTestHelper helper) {
		Fixture fixture = createFixture(helper);
		MerchantOffers offers = fixture.villager().getOffers();
		offers.getFirst().increaseUses();
		check(!TradeCyclingService.cycle(fixture.player(), fixture.menu().containerId), "Used trades cannot be rerolled");
		check(fixture.villager().getOffers() == offers, "Rejected cycling must preserve offers");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void rejectsXpAndLeveledVillagers(final GameTestHelper helper) {
		Fixture fixture = createFixture(helper);
		fixture.villager().setVillagerXp(1);
		check(!TradeCyclingService.cycle(fixture.player(), fixture.menu().containerId), "Any villager XP locks cycling");
		fixture.villager().setVillagerXp(0);
		fixture.villager().setVillagerData(fixture.villager().getVillagerData().setLevel(2));
		check(!TradeCyclingService.cycle(fixture.player(), fixture.menu().containerId), "Non-novices cannot cycle");
		helper.succeed();
	}

	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
	public void rejectsInvalidSessionsAndKeepsOffers(final GameTestHelper helper) {
		Fixture fixture = createFixture(helper);
		MerchantOffers offers = fixture.villager().getOffers();
		check(!TradeCyclingService.cycle(fixture.player(), fixture.menu().containerId + 1), "Reject mismatched container IDs");
		fixture.villager().setTradingPlayer(null);
		check(!TradeCyclingService.cycle(fixture.player(), fixture.menu().containerId), "Only the villager's trading player may cycle");
		fixture.villager().setTradingPlayer(fixture.player());
		fixture.player().setPos(fixture.villager().getX() + 100, fixture.villager().getY(), fixture.villager().getZ());
		check(!TradeCyclingService.cycle(fixture.player(), fixture.menu().containerId), "Reject out-of-range players");
		check(fixture.villager().getOffers() == offers, "Invalid requests must not mutate trades");
		helper.succeed();
	}

	private static Fixture createFixture(final GameTestHelper helper) {
		@SuppressWarnings("unchecked")
		EntityType<Villager> villagerType = (EntityType<Villager>)(EntityType<?>)BuiltInRegistries.ENTITY_TYPE.get(
			new ResourceLocation("minecraft", "villager")
		);
		Villager villager = helper.spawnWithNoFreeWill(villagerType, 1, 2, 1);
		villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.LIBRARIAN));
		TestPlayer player = new TestPlayer(helper.getLevel());
		player.setPos(villager.getX() + 1, villager.getY(), villager.getZ());
		villager.setTradingPlayer(player);
		MerchantMenu menu = new MerchantMenu(7, player.getInventory(), villager);
		player.containerMenu = menu;
		villager.getOffers();
		return new Fixture(villager, player, menu);
	}

	private static void check(final boolean condition, final String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private record Fixture(Villager villager, TestPlayer player, MerchantMenu menu) {
	}

	private static final class TestPlayer extends ServerPlayer {
		private MerchantOffers lastSentOffers;

		private TestPlayer(final ServerLevel level) {
			super(level.getServer(), level, new GameProfile(UUID.randomUUID(), "cycling-test"), ClientInformation.createDefault());
		}

		@Override
		public void sendMerchantOffers(
			final int containerId, final MerchantOffers offers, final int merchantLevel, final int merchantXp,
			final boolean showProgress, final boolean canRestock
		) {
			this.lastSentOffers = offers;
		}
	}
}
