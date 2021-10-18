package snownee.kaleido.brush.item;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

import moe.mmf.csscolors.Color;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kaleido.Kaleido;
import snownee.kaleido.brush.network.CConfigureBrushPacket;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.mixin.MixinBlockColors;
import snownee.kiwi.item.ModItem;

public class BrushItem extends ModItem {

	public BrushItem(Properties builder) {
		super(builder.stacksTo(1).tab(ItemGroup.TAB_TOOLS));
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void pick(ClickInputEvent event) {
		if (!event.isPickBlock())
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || mc.hitResult == null || mc.hitResult.getType() == Type.ENTITY)
			return;
		ClientPlayerEntity player = mc.player;
		ItemStack stack = player.getItemInHand(event.getHand());
		if (!(stack.getItem() instanceof BrushItem))
			return;
		event.setCanceled(true);
		String oldKey = getTint(stack);
		String key;
		if (mc.hitResult.getType() == Type.BLOCK) {
			BlockRayTraceResult hitResult = (BlockRayTraceResult) mc.hitResult;
			BlockPos pos = hitResult.getBlockPos();
			BlockState state = player.level.getBlockState(pos);
			Color color = null;
			IBlockColor blockColor = ((MixinBlockColors) mc.getBlockColors()).getBlockColors().get(state.getBlock().delegate);
			if (blockColor == null || Kaleido.isKaleidoBlock(state)) {
				TileEntity blockEntity = player.level.getBlockEntity(pos);
				if (blockEntity instanceof MasterBlockEntity) {
					MasterBlockEntity master = (MasterBlockEntity) blockEntity;
					ModelInfo info = master.getModelInfo();
					if (info == null || info.tint == null) {
						return;
					}
					int index = getIndex(stack);
					index = MathHelper.clamp(index, 0, info.tint.length - 1);
					if (master.tint != null && master.tint.length == info.tint.length && master.tint[index] != null) {
						key = master.tint[index];
					} else {
						key = info.tint[index];
					}
				} else {
					BlockDefinition definition = BlockDefinition.fromBlock(state, blockEntity, player.level, pos);
					int col = BlockDefinition.getCamo(definition).getBlockState().getMapColor(player.level, pos).col;
					color = fromInt(col);
					key = color.toHex();
					KaleidoClient.ITEM_COLORS.ensureCache(key);
				}
			} else {
				key = state.getBlock().getRegistryName().toString();
			}
			if (oldKey != null) {
				if (color == null) {
					color = fromInt(KaleidoClient.ITEM_COLORS.getColor(key, stack, 0));
				}
				int oldColor = KaleidoClient.ITEM_COLORS.getColor(oldKey, stack, 0);
				color = mix(fromInt(oldColor), color);
				key = color.toHex();
				KaleidoClient.ITEM_COLORS.ensureCache(key);
			}
		} else { // MISS
			key = null;
		}
		if (!Objects.equal(oldKey, key)) {
			SoundEvent sound = key == null ? SoundEvents.BUCKET_EMPTY : SoundEvents.ITEM_PICKUP;
			player.level.playSound(player, player.getX(), player.getY() + 0.5, player.getZ(), sound, SoundCategory.PLAYERS, 0.2F, ((player.level.random.nextFloat() - player.level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			new CConfigureBrushPacket(event.getHand(), key).send();
		}
	}

	@Override
	public boolean canAttackBlock(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
		return false;
	}

	@Nullable
	public static String getTint(ItemStack stack) {
		if (stack.hasTag()) {
			return stack.getTag().getString("Tint");
		}
		return null;
	}

	public static int getIndex(ItemStack stack) {
		int index = 0;
		if (stack.hasTag()) {
			//			index = stack.getTag().getInt("Index");
			if (stack.hasCustomHoverName()) {
				String name = stack.getHoverName().getString();
				try {
					index = Integer.valueOf(name);
				} catch (Throwable e) {
				}
			}
		}
		return index;
	}

	public static Color mix(Color color1, Color color2) {
		float aR = (color1.R() + color2.R()) / 2;
		float aG = (color1.G() + color2.G()) / 2;
		float aB = (color1.B() + color2.B()) / 2;
		float aMaximum = (max(color1.R(), color1.G(), color1.B()) + max(color2.R(), color2.G(), color2.B())) / 2;
		float maximumOfAverage = max(aR, aG, aB);
		float factor = aMaximum / maximumOfAverage;
		byte R = (byte) (aR * factor);
		byte G = (byte) (aG * factor);
		byte B = (byte) (aB * factor);
		return Color.fromRGB(R, G, B);
	}

	public static float max(float r, float g, float b) {
		return Math.max(r, Math.max(g, b));
	}

	public static Color fromInt(int col) {
		byte r = (byte) (col >> 16 & 255);
		byte g = (byte) (col >> 8 & 255);
		byte b = (byte) (col & 255);
		return Color.fromRGB(r, g, b);
	}

}
