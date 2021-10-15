package snownee.kaleido.scope.network;

import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.network.NetworkEvent.Context;
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

		@Override
		public void handle(CCreateScopePacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity player = ctx.get().getSender();
				RayTraceResult hitResult = player.pick(KaleidoUtil.getPickRange(player), 0, false);
				if (hitResult instanceof BlockRayTraceResult) {
					BlockPos pos = ((BlockRayTraceResult) hitResult).getBlockPos();
					BlockState state = player.level.getBlockState(pos);
					if (state.is(ScopeModule.SCOPE)) {
						return;
					}
					if (!KaleidoUtil.canPlayerBreak(player, state, pos)) {
						return;
					}
					TileEntity blockEntity0 = player.level.getBlockEntity(pos);
					player.level.setBlockAndUpdate(pos, ScopeModule.SCOPE.defaultBlockState());
					TileEntity blockEntity = player.level.getBlockEntity(pos);
					if (blockEntity instanceof ScopeBlockEntity) {
						((ScopeBlockEntity) blockEntity).addStack(BlockDefinition.fromBlock(state, blockEntity0, player.level, pos), player);
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}

	}

}
