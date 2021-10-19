package snownee.kaleido.datagen;

import java.util.stream.Collectors;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.SnowBlock;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.BlockStateProperty;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.loot.functions.SetCount;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kaleido.Kaleido;
import snownee.kaleido.carpentry.CarpentryModule;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoLootFunction;
import snownee.kaleido.scope.ScopeModule;

public class KaleidoBlockLoot extends BlockLootTables {

	@Override
	protected void addTables() {
		dropSelf(CarpentryModule.WOODWORKING_BENCH);

		add(ScopeModule.SCOPE, noDrop());

		add(ChiselModule.CHISELED_FENCE, chiseled(createSingleItemTable(ChiselModule.CHISELED_FENCE)));
		add(ChiselModule.CHISELED_FENCE_GATE, chiseled(createSingleItemTable(ChiselModule.CHISELED_FENCE_GATE)));
		add(ChiselModule.CHISELED_WALL, chiseled(createSingleItemTable(ChiselModule.CHISELED_WALL)));
		add(ChiselModule.CHISELED_STAIRS, chiseled(createSingleItemTable(ChiselModule.CHISELED_STAIRS)));
		add(ChiselModule.CHISELED_VSLAB, chiseled(createSingleItemTable(ChiselModule.CHISELED_VSLAB)));
		add(ChiselModule.CHISELED_SLAB, chiseled(createSlabItemTable(ChiselModule.CHISELED_SLAB)));
		add(ChiselModule.CHISELED_LAYERS, chiseled(createLayersItemTable(ChiselModule.CHISELED_LAYERS)));

		for (Block block : CoreModule.MASTER_BLOCKS)
			add(block, createSingleItemTable(block).apply(KaleidoLootFunction.create()));
	}

	public static LootTable.Builder chiseled(LootTable.Builder builder) {
		return builder.apply(CopyNbt.copyData(CopyNbt.Source.BLOCK_ENTITY).copy("Overrides", "BlockEntityTag.Overrides"));
	}

	public static LootTable.Builder createLayersItemTable(Block block) {
		applyExplosionDecay(block, LootTable.lootTable().withPool(LootPool.lootPool().add(ItemLootEntry.lootTableItem(block))));
		LootTable.Builder lootTable = createSingleItemTable(block);
		for (int i = 2; i <= 8; i++) {
			lootTable.apply(SetCount.setCount(ConstantRange.exactly(i)).when(BlockStateProperty.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowBlock.LAYERS, i))));
		}
		return lootTable;
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return ForgeRegistries.BLOCKS.getValues().stream().filter($ -> Kaleido.MODID.equals($.getRegistryName().getNamespace())).collect(Collectors.toList());
	}

}
