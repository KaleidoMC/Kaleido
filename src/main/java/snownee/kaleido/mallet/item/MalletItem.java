package snownee.kaleido.mallet.item;

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.block.HugeMushroomBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kaleido.chisel.block.ChiseledBlockEntity;
import snownee.kaleido.chisel.block.LayersBlock;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.mallet.MalletModule;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kiwi.item.ModItem;

// DebugStickItem
public class MalletItem extends ModItem {

	public MalletItem(Properties builder) {
		super(builder.stacksTo(1).tab(ItemGroup.TAB_TOOLS));
	}

	@Override
	public boolean canAttackBlock(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
		if (!pLevel.isClientSide) {
			handleInteraction(pPlayer, pState, pLevel, pPos, false, pPlayer.getItemInHand(Hand.MAIN_HAND));
		}
		return false;
	}

	@Override
	public ActionResultType useOn(ItemUseContext pContext) {
		PlayerEntity playerentity = pContext.getPlayer();
		World world = pContext.getLevel();
		if (!world.isClientSide && playerentity != null) {
			BlockPos blockpos = pContext.getClickedPos();
			handleInteraction(playerentity, world.getBlockState(blockpos), world, blockpos, true, pContext.getItemInHand());
		}

		return ActionResultType.sidedSuccess(world.isClientSide);
	}

	private static void handleInteraction(PlayerEntity player, BlockState state, World world, BlockPos pos, boolean rightClick, ItemStack stack) {
		if (!KaleidoUtil.canPlayerBreak(player, state, pos)) {
			return;
		}
		if (!canApplyOn(state, world, pos)) {
			KaleidoUtil.displayClientMessage(player, false, "msg.kaleido.malletUnsupported");
			return;
		}
		Block block = state.getBlock();
		StateContainer<Block, BlockState> statecontainer = block.getStateDefinition();
		Collection<Property<?>> collection = Lists.newArrayList(statecontainer.getProperties());
		collection.remove(BlockStateProperties.WATERLOGGED);
		collection.remove(LayersBlock.LAYERS);
		collection.remove(SlabBlock.TYPE);
		@SuppressWarnings("deprecation")
		String s = Registry.BLOCK.getKey(block).toString();
		if (collection.isEmpty()) {
			KaleidoUtil.displayClientMessage(player, false, Items.DEBUG_STICK.getDescriptionId() + ".empty", s);
			return;
		}
		CompoundNBT compoundnbt = stack.getOrCreateTagElement("DebugProperty");
		String s1 = compoundnbt.getString(s);
		Property<?> property = statecontainer.getProperty(s1);
		if (rightClick) {
			if (property == null) {
				property = collection.iterator().next();
			}

			BlockState blockstate = cycleState(state, property, player.isSecondaryUseActive());
			world.setBlock(pos, blockstate, 18);
			KaleidoUtil.displayClientMessage(player, false, Items.DEBUG_STICK.getDescriptionId() + ".update", property.getName(), getNameHelper(blockstate, property));
		} else {
			property = getRelative(collection, property, player.isSecondaryUseActive());
			String s2 = property.getName();
			compoundnbt.putString(s, s2);
			KaleidoUtil.displayClientMessage(player, false, Items.DEBUG_STICK.getDescriptionId() + ".select", s2, getNameHelper(state, property));
		}
	}

	private static <T extends Comparable<T>> BlockState cycleState(BlockState pState, Property<T> pProperty, boolean pBackwards) {
		return pState.setValue(pProperty, getRelative(pProperty.getPossibleValues(), pState.getValue(pProperty), pBackwards));
	}

	private static <T> T getRelative(Iterable<T> pAllowedValues, @Nullable T pCurrentValue, boolean pBackwards) {
		return pBackwards ? Util.findPreviousInIterable(pAllowedValues, pCurrentValue) : Util.findNextInIterable(pAllowedValues, pCurrentValue);
	}

	private static <T extends Comparable<T>> String getNameHelper(BlockState pState, Property<T> pProperty) {
		return pProperty.getName(pState.getValue(pProperty));
	}

	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getPlayer().isCreative()) {
			return;
		}
		ItemStack stack = event.getItemStack();
		if (!(stack.getItem() instanceof MalletItem))
			return;
		event.setCanceled(true);
		CooldownTracker cooldowns = event.getPlayer().getCooldowns();
		if (cooldowns.isOnCooldown(stack.getItem())) {
			return;
		}
		World worldIn = event.getWorld();
		BlockPos pos = event.getPos();
		BlockState state = worldIn.getBlockState(pos);
		stack.getItem().canAttackBlock(state, worldIn, pos, event.getPlayer());
		cooldowns.addCooldown(stack.getItem(), 10);
	}

	public static boolean canApplyOn(BlockState state, World world, BlockPos pos) {
		if (state.hasTileEntity()) {
			TileEntity blockEntity = world.getBlockEntity(pos);
			return blockEntity instanceof ChiseledBlockEntity || blockEntity instanceof MasterBlockEntity;
		}
		Block block = state.getBlock();
		if (block instanceof FenceBlock || block instanceof PaneBlock || block instanceof WallBlock || block instanceof StairsBlock || block instanceof HugeMushroomBlock || block instanceof GlazedTerracottaBlock || block instanceof FenceGateBlock || block instanceof TrapDoorBlock || block instanceof VineBlock || block instanceof RotatedPillarBlock) {
			return true;
		}
		return state.is(MalletModule.MALLEABLE);
	}

}
