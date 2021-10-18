package snownee.kaleido.hammer.item;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import snownee.kiwi.item.ModItem;

// DebugStickItem
public class HammerItem extends ModItem {

	public HammerItem(Properties builder) {
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

	@SuppressWarnings("deprecation")
	private void handleInteraction(PlayerEntity p_195958_1_, BlockState p_195958_2_, IWorld p_195958_3_, BlockPos p_195958_4_, boolean p_195958_5_, ItemStack p_195958_6_) {
		if (p_195958_1_.canUseGameMasterBlocks()) {
			Block block = p_195958_2_.getBlock();
			StateContainer<Block, BlockState> statecontainer = block.getStateDefinition();
			Collection<Property<?>> collection = statecontainer.getProperties();
			String s = Registry.BLOCK.getKey(block).toString();
			if (collection.isEmpty()) {
				message(p_195958_1_, new TranslationTextComponent(this.getDescriptionId() + ".empty", s));
			} else {
				CompoundNBT compoundnbt = p_195958_6_.getOrCreateTagElement("DebugProperty");
				String s1 = compoundnbt.getString(s);
				Property<?> property = statecontainer.getProperty(s1);
				if (p_195958_5_) {
					if (property == null) {
						property = collection.iterator().next();
					}

					BlockState blockstate = cycleState(p_195958_2_, property, p_195958_1_.isSecondaryUseActive());
					p_195958_3_.setBlock(p_195958_4_, blockstate, 18);
					message(p_195958_1_, new TranslationTextComponent(this.getDescriptionId() + ".update", property.getName(), getNameHelper(blockstate, property)));
				} else {
					property = getRelative(collection, property, p_195958_1_.isSecondaryUseActive());
					String s2 = property.getName();
					compoundnbt.putString(s, s2);
					message(p_195958_1_, new TranslationTextComponent(this.getDescriptionId() + ".select", s2, getNameHelper(p_195958_2_, property)));
				}

			}
		}
	}

	private static <T extends Comparable<T>> BlockState cycleState(BlockState pState, Property<T> pProperty, boolean pBackwards) {
		return pState.setValue(pProperty, getRelative(pProperty.getPossibleValues(), pState.getValue(pProperty), pBackwards));
	}

	private static <T> T getRelative(Iterable<T> pAllowedValues, @Nullable T pCurrentValue, boolean pBackwards) {
		return pBackwards ? Util.findPreviousInIterable(pAllowedValues, pCurrentValue) : Util.findNextInIterable(pAllowedValues, pCurrentValue);
	}

	private static void message(PlayerEntity pPlayer, ITextComponent pMessageComponent) {
		((ServerPlayerEntity) pPlayer).sendMessage(pMessageComponent, ChatType.GAME_INFO, Util.NIL_UUID);
	}

	private static <T extends Comparable<T>> String getNameHelper(BlockState pState, Property<T> pProperty) {
		return pProperty.getName(pState.getValue(pProperty));
	}
}
