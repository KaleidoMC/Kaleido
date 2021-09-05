package snownee.kaleido.carpentry;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.world.server.ServerWorld;
import snownee.kaleido.core.CoreModule;

public class CollectorTrade implements ITrade {

    private Supplier<LootTable> lootTableSupplier;

    public CollectorTrade(Supplier<LootTable> lootTableSupplier) {
        this.lootTableSupplier = lootTableSupplier;
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, Random rand) {
        if (!(trader.level instanceof ServerWorld)) {
            return null;
        }
        LootContext context = new LootContext.Builder((ServerWorld) trader.level).withRandom(rand).create(LootParameterSets.EMPTY);
        List<ItemStack> stacks = lootTableSupplier.get().getRandomItems(context);
        if (stacks.isEmpty()) {
            return null;
        }
        Item coin = CoreModule.CLOTH_TAG.getRandomElement(rand);
        ItemStack coins = new ItemStack(coin, 4 + rand.nextInt(3));
        return new MerchantOffer(stacks.get(0), coins, 1, 2, 1);
    }

}
