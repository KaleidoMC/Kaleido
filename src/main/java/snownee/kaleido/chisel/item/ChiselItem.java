package snownee.kaleido.chisel.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kaleido.Kaleido;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kaleido.chisel.ChiselPalette;
import snownee.kaleido.chisel.block.entity.ChiseledBlockEntity;
import snownee.kaleido.chisel.network.CChiselClickPacket;
import snownee.kaleido.chisel.network.CChiselPickPacket;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kiwi.item.ModItem;
import snownee.kiwi.util.NBTHelper;

public class ChiselItem extends ModItem {

	public ChiselItem(Properties builder) {
		super(builder.stacksTo(1).tab(ItemGroup.TAB_TOOLS));
	}

	public static ChiselPalette palette(ItemStack stack) {
		return ChiselPalette.byName(NBTHelper.of(stack).getString("Palette"));
	}

	@Override
	public boolean canAttackBlock(BlockState state, World level, BlockPos pos, PlayerEntity player) {
		return false;
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		ItemStack stack = event.getItemStack();
		if (!(stack.getItem() instanceof ChiselItem))
			return;
		event.setCanceled(true);
		CooldownTracker cooldowns = event.getPlayer().getCooldowns();
		if (cooldowns.isOnCooldown(stack.getItem())) {
			return;
		}
		new CChiselClickPacket(event.getHand()).send();
	}

	public static void click(PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hitResult) {
		BlockPos pos = hitResult.getBlockPos();
		BlockState state = player.level.getBlockState(pos);
		if (!KaleidoUtil.canPlayerBreak(player, state, pos)) {
			return;
		}
		World level = player.level;
		boolean isChiseled = ChiselModule.CHISELED_BLOCKS.contains(state.getBlock());
		if (!isChiseled) {
			if (Kaleido.isKaleidoBlock(state) && !Block.isShapeFullBlock(state.getCollisionShape(level, pos))) {
				return;
			} else if (!Block.isShapeFullBlock(state.getShape(level, pos))) {
				return;
			}
		}
		ChiselPalette palette = palette(stack);
		if (palette == ChiselPalette.NONE) {
			palette = ChiselPalette.byBlock(state).next();
			if (level.isClientSide && palette != ChiselPalette.NONE)
				player.displayClientMessage(palette.chiseledBlock.getName(), true); //TODO better name
		}
		BlockDefinition supplier;
		BlockItemUseContext context = new BlockItemUseContext(player, Hand.MAIN_HAND, stack, hitResult);
		context.replaceClicked = true;
		TileEntity blockEntity = level.getBlockEntity(pos);
		if (isChiseled) {
			if (!(blockEntity instanceof ChiseledBlockEntity))
				return;
			supplier = ((ChiseledBlockEntity) blockEntity).getTexture();
		} else {
			supplier = BlockDefinition.fromBlock(state, blockEntity, level, pos);
		}
		if (supplier == null) {
			return;
		}
		CooldownTracker cooldowns = player.getCooldowns();
		if (cooldowns.isOnCooldown(stack.getItem())) {
			return;
		}
		palette.place(supplier, level, pos, context);
		SoundType sound = level.getBlockState(pos).getSoundType(level, pos, player);
		level.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
		cooldowns.addCooldown(stack.getItem(), 3);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void pick(ClickInputEvent event) {
		if (!event.isPickBlock())
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || mc.hitResult == null || mc.hitResult.getType() != Type.BLOCK)
			return;
		ItemStack stack = mc.player.getItemInHand(event.getHand());
		if (!(stack.getItem() instanceof ChiselItem))
			return;
		event.setCanceled(true);
		pick(mc.player, event.getHand(), stack, (BlockRayTraceResult) mc.hitResult);
	}

	public static void pick(PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hitResult) {
		BlockPos pos = hitResult.getBlockPos();
		BlockState state = player.level.getBlockState(pos);
		CompoundNBT tag = stack.getOrCreateTag();
		CompoundNBT tag1 = tag.copy();
		ChiselPalette palette = ChiselPalette.pick(state);
		tag.putString("Palette", palette.name);
		BlockDefinition definition = BlockDefinition.fromBlock(state, player.level.getBlockEntity(pos), player.level, pos);
		if (definition != null) {
			CompoundNBT defTag = new CompoundNBT();
			definition.save(defTag);
			defTag.putString("Type", definition.getFactory().getId());
			tag.put("Def", defTag);
		}
		if (player.level.isClientSide && !tag.equals(tag1)) {
			if (palette != ChiselPalette.NONE) {
				player.displayClientMessage(palette.chiseledBlock.getName(), true); //TODO better name
			}
			player.level.playSound(player, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.level.random.nextFloat() - player.level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			new CChiselPickPacket(hand).send();
		}
	}
}
