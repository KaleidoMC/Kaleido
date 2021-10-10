package snownee.kaleido.chisel.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kaleido.Kaleido;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kaleido.chisel.ChiselPalette;
import snownee.kaleido.chisel.block.ChiseledBlockEntity;
import snownee.kaleido.chisel.network.CSetPalettePacket;
import snownee.kaleido.core.supplier.ModelSupplier;
import snownee.kiwi.item.ModItem;
import snownee.kiwi.util.NBTHelper;

public class ChiselItem extends ModItem {

	static {
		MinecraftForge.EVENT_BUS.register(ChiselItem.class);
	}

	public ChiselItem(Properties builder) {
		super(builder.stacksTo(1).tab(ItemGroup.TAB_TOOLS));
	}

	public static ChiselPalette palette(ItemStack stack) {
		return ChiselPalette.byName(NBTHelper.of(stack).getString("Palette"));
	}

	@Override
	public boolean canAttackBlock(BlockState state, World level, BlockPos pos, PlayerEntity player) {
		boolean isChiseled = ChiselModule.CHISELED_BLOCKS.contains(state.getBlock());
		if (!isChiseled) {
			if (Kaleido.isKaleidoBlock(state) && !Block.isShapeFullBlock(state.getCollisionShape(level, pos))) {
				return false;
			} else if (!Block.isShapeFullBlock(state.getShape(level, pos))) {
				return false;
			}
		}
		if (!level.isClientSide) {
			ItemStack stack = player.getMainHandItem();
			BlockRayTraceResult hitResult = getPlayerPOVHitResult(level, player, FluidMode.NONE);
			if (hitResult.getType() == Type.MISS) {
				return false;
			}
			ChiselPalette palette = palette(stack);
			if (palette == ChiselPalette.NONE) {
				palette = ChiselPalette.byBlock(state).next();
				if (palette != ChiselPalette.NONE)
					player.displayClientMessage(palette.chiseledBlock.getName(), true); //TODO better name
			}
			ModelSupplier supplier;
			BlockItemUseContext context = new BlockItemUseContext(player, Hand.MAIN_HAND, stack, hitResult);
			context.replaceClicked = true;
			if (isChiseled) {
				TileEntity blockEntity = level.getBlockEntity(pos);
				if (!(blockEntity instanceof ChiseledBlockEntity))
					return false;
				supplier = ((ChiseledBlockEntity) blockEntity).getTexture();
			} else {
				supplier = ModelSupplier.fromBlock(state, level, pos);
			}
			if (supplier != null) {
				palette.place(supplier, level, pos, context);
			}
		}
		return false;
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
		BlockPos pos = ((BlockRayTraceResult) mc.hitResult).getBlockPos();
		BlockState state = mc.level.getBlockState(pos);
		ChiselPalette palette = ChiselPalette.pick(state);
		ChiselPalette palette1 = palette(stack);
		if (palette == palette1)
			return;
		if (palette != ChiselPalette.NONE) {
			mc.player.displayClientMessage(palette.chiseledBlock.getName(), true); //TODO better name
			mc.level.playSound(mc.player, mc.player.getX(), mc.player.getY() + 0.5, mc.player.getZ(), SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((mc.level.random.nextFloat() - mc.level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
		}
		stack.getOrCreateTag().putString("Palette", palette.name);
		new CSetPalettePacket(event.getHand(), palette).send();
	}
}
