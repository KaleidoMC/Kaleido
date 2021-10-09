package snownee.kaleido.chisel;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.kaleido.chisel.block.RetextureBlockEntity;
import snownee.kaleido.chisel.block.VSlabBlock;
import snownee.kaleido.core.supplier.ModelSupplier;

public class ChiselPalette {
	private static final Map<String, ChiselPalette> byName = Maps.newHashMap();
	private static final Map<Block, ChiselPalette> byBlock = Maps.newHashMap();
	public static final ChiselPalette NONE = new ChiselPalette("None", Blocks.AIR, Predicates.alwaysFalse());
	private static ChiselPalette last;

	public final String name;
	private final Block chiseledBlock;
	private final Predicate<BlockState> pickPred;
	private ItemStack icon;
	private ChiselPalette next;

	public ChiselPalette(String name, Block chiseledBlock, Predicate<BlockState> pickPred) {
		this.name = name;
		this.chiseledBlock = chiseledBlock;
		this.pickPred = pickPred;
		byName.put(name, this);
		if (chiseledBlock != Blocks.AIR)
			byBlock.put(chiseledBlock, this);
		if (last != null)
			last.next = this;
		last = this;
	}

	public ChiselPalette next() {
		return next == null ? NONE : next;
	}

	public ItemStack icon() {
		if (icon == null)
			icon = new ItemStack(chiseledBlock);
		return icon;
	}

	public void place(ModelSupplier supplier, World level, BlockPos pos, BlockItemUseContext context) {
		if (this == NONE) {
			supplier.place(level, pos);
			return;
		}
		BlockState state2 = chiseledBlock.getStateForPlacement(context);
		level.setBlockAndUpdate(pos, state2);
		TileEntity tile = level.getBlockEntity(pos);
		if (tile instanceof RetextureBlockEntity) {
			RetextureBlockEntity textureTile = (RetextureBlockEntity) tile;
			textureTile.setTexture("0", supplier);
			textureTile.refresh();
		}
	}

	public static void init() {
		new ChiselPalette("Stairs", ChiselModule.CHISELED_STAIRS, state -> state.is(BlockTags.STAIRS));
		new ChiselPalette("Slab", ChiselModule.CHISELED_SLAB, state -> state.is(BlockTags.SLABS));
		new ChiselPalette("VSlab", ChiselModule.CHISELED_VSLAB, state -> state.getBlock() instanceof VSlabBlock);
		new ChiselPalette("Wall", ChiselModule.CHISELED_WALL, state -> state.is(BlockTags.WALLS));
		new ChiselPalette("Fence", ChiselModule.CHISELED_FENCE, state -> state.is(BlockTags.FENCES));
		new ChiselPalette("FenceGate", ChiselModule.CHISELED_FENCE_GATE, state -> state.is(BlockTags.FENCE_GATES));
	}

	public static ChiselPalette byBlock(BlockState state) {
		return byBlock.getOrDefault(state.getBlock(), NONE);
	}

	public static ChiselPalette byName(String name) {
		return byName.getOrDefault(name, NONE);
	}

	public static ChiselPalette pick(BlockState state) {
		for (ChiselPalette palette : byName.values()) {
			if (palette.pickPred.test(state)) {
				return palette;
			}
		}
		return NONE;
	}

}
