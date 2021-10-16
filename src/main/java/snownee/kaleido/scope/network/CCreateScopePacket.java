package snownee.kaleido.scope.network;

import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.chisel.item.ChiselItem;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.scope.ScopeModule;
import snownee.kaleido.scope.block.ScopeBlockEntity;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kiwi.network.ClientPacket;

public class CCreateScopePacket extends ClientPacket {

	public static class Handler extends PacketHandler<CCreateScopePacket> {

		@Override
		public CCreateScopePacket decode(PacketBuffer buf) {
			return new CCreateScopePacket();
		}

		@Override
		public void encode(CCreateScopePacket pkt, PacketBuffer buf) {
		}

		@SuppressWarnings("deprecation")
		@Override
		public void handle(CCreateScopePacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity player = ctx.get().getSender();
				RayTraceResult hitResult = player.pick(KaleidoUtil.getPickRange(player), 0, false);
				if (hitResult instanceof BlockRayTraceResult) {
					BlockPos pos = ((BlockRayTraceResult) hitResult).getBlockPos();
					ItemStack stack = player.getMainHandItem();
					boolean chisel = stack.getItem() instanceof ChiselItem;
					if (chisel) {
						pos = pos.relative(((BlockRayTraceResult) hitResult).getDirection());
					}
					BlockState state = player.level.getBlockState(pos);
					if (!KaleidoUtil.canPlayerBreak(player, state, pos)) {
						return;
					}
					if (chisel) {
						BlockItemUseContext useContext = new BlockItemUseContext(player, Hand.MAIN_HAND, stack, (BlockRayTraceResult) hitResult);
						if (!state.canBeReplaced(useContext)) {
							return;
						}
					} else {
						if (state.isAir() || state.is(ScopeModule.SCOPE)) {
							return;
						}
					}
					TileEntity blockEntity0 = player.level.getBlockEntity(pos);
					BlockDefinition definition;
					if (chisel) {
						definition = BlockDefinition.fromNBT(stack.getTagElement("Def"));
					} else {
						definition = BlockDefinition.fromBlock(state, blockEntity0, player.level, pos);
					}
					if (definition != null) {
						player.level.setBlockAndUpdate(pos, ScopeModule.SCOPE.defaultBlockState());
						TileEntity blockEntity = player.level.getBlockEntity(pos);
						if (blockEntity instanceof ScopeBlockEntity) {
							if (!chisel) {
								((ScopeBlockEntity) blockEntity).fromLevel = true;
							}
							((ScopeBlockEntity) blockEntity).addStack(definition, player);
						}
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}

	}

}
