package snownee.kaleido.carpentry;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import snownee.kaleido.carpentry.block.WoodworkingBenchBlock;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.Name;
import snownee.kiwi.item.ModItem;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.NBTHelper.NBT;

@KiwiModule.Subscriber
@KiwiModule("carpentry")
@KiwiModule.Group("decorations")
@KiwiModule.Optional
public class CarpentryModule extends AbstractModule {

	public static final Item CLOTH = new ModItem(itemProp());

	public static final WoodworkingBenchBlock WOODWORKING_BENCH = new WoodworkingBenchBlock(blockProp(Material.WOOD));

	@Name("collector")
	public static final PointOfInterestType COLLECTOR_POI = PointOfInterestType.registerBlockStates(new PointOfInterestType("kaleido.collector", PointOfInterestType.getBlockStates(WOODWORKING_BENCH), 1, 1));

	public static final VillagerProfession COLLECTOR = new VillagerProfession("kaleido.collector", COLLECTOR_POI, ImmutableSet.of(), ImmutableSet.of(), null);

	private CollectorTrade trade;

	@SubscribeEvent
	public void addVillagerTrades(VillagerTradesEvent event) {
		if (event.getType() == COLLECTOR) {
			Supplier<LootTable> lootTableSupplier = () -> ServerLifecycleHooks.getCurrentServer().getLootTables().get(RL("gameplay/collector"));
			trade = new CollectorTrade(lootTableSupplier);
			// event.getTrades().put(1, ImmutableList.of(trade, trade));
			// event.getTrades().put(1, ImmutableList.of(new BasicTrade(1, new ItemStack(Items.DIAMOND), 10, 1)));
		}
	}

	@SubscribeEvent
	public void tickVillager(LivingUpdateEvent event) {
		if (!(event.getEntityLiving() instanceof VillagerEntity)) {
			return;
		}
		VillagerEntity villager = (VillagerEntity) event.getEntityLiving();
		if (villager.getVillagerData().getProfession() != COLLECTOR) {
			return;
		}
		if (villager.lastTradedPlayer instanceof ServerPlayerEntity) {
			// Refresh quests
			int noUses = 0;
			for (MerchantOffer offer : villager.getOffers()) {
				offer.getBaseCostA();
				if (offer.isOutOfStock()) {
					noUses++;
				}
			}
			villager.getOffers().removeIf(MerchantOffer::isOutOfStock);
			addNewTrades(villager, noUses);

			// Check qualification
			long day = villager.lastTradedPlayer.level.getDayTime() / 24000L;
			NBTHelper data = NBTHelper.of(villager.getPersistentData());
			ListNBT list = data.getTagList("Kaleido.Customers", NBT.COMPOUND);
			if (list == null) {
				data.setTag("Kaleido.Customers", list = new ListNBT());
			}
			if (data.getLong("Kaleido.Day") < day) {
				list.clear();
			} else {
				for (INBT nbt : list) {
					if (nbt instanceof CompoundNBT) {
						UUID uuid = NBTUtil.loadUUID(nbt);
						if (villager.lastTradedPlayer.getUUID().equals(uuid)) {
							return;
						}
					}
				}
			}
			list.add(NBTUtil.createUUID(villager.lastTradedPlayer.getUUID()));
			data.setLong("Kaleido.Day", day);

			// Unlock
			AxisAlignedBB bb = new AxisAlignedBB(villager.position().subtract(5, 5, 5), villager.position().add(5, 5, 5));
			List<ServerPlayerEntity> players = villager.level.getEntitiesOfClass(ServerPlayerEntity.class, bb, $ -> !$.isSpectator());
			ModelInfo info = KaleidoDataManager.INSTANCE.getRandomLocked((ServerPlayerEntity) villager.lastTradedPlayer, villager.lastTradedPlayer.getRandom());
			if (info != null) {
				for (ServerPlayerEntity player : players) {
					info.grant(player);
				}
			}
		}

	}

	@SubscribeEvent
	public void addExtraTrades(EntityInteractSpecific event) {
		if (event.getWorld().isClientSide || !(event.getTarget() instanceof VillagerEntity)) {
			return;
		}
		VillagerEntity villager = (VillagerEntity) event.getTarget();
		if (villager.getVillagerData().getProfession() != COLLECTOR || villager.getVillagerData().getLevel() != 1) {
			return;
		}
		if (villager.getOffers().size() < 4) {
			addNewTrades(villager, 4 - villager.getOffers().size());
		}
	}

	public void addNewTrades(VillagerEntity villager, int amount) {
		MerchantOffers offers = villager.getOffers();
		Set<Item> existingItems = offers.stream().map(MerchantOffer::getBaseCostA).map(ItemStack::getItem).collect(Collectors.toSet());
		int failed = 0;
		while (amount > 0 && failed < 50) {
			MerchantOffer newOffer = trade.getOffer(villager, villager.getRandom());
			if (newOffer == null || existingItems.contains(newOffer.getBaseCostA().getItem())) {
				++failed;
			} else {
				offers.add(newOffer);
				--amount;
			}
		}
	}

}
