package snownee.kaleido.core.action;

import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.util.KaleidoTemplate;

public class TransformAction implements Consumer<ActionContext> {

	public static TransformAction create(JsonObject obj) {
		return new TransformAction(new ResourceLocation(JSONUtils.getAsString(obj, "block")));
	}

	private ResourceLocation blockTo;

	public TransformAction(ResourceLocation blockTo) {
		this.blockTo = blockTo;
	}

	@Override
	public void accept(ActionContext ctx) {
		World level = ctx.getLevel();
		BlockPos pos = ctx.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if ("kaleido".equals(blockTo.getNamespace())) {
			ModelInfo info = KaleidoDataManager.get(blockTo);
			if (info == null || info.template == KaleidoTemplate.item)
				return;
			level.setBlockAndUpdate(pos, tryCopyBlockState(state, info.template.bloc.defaultBlockState()));
			TileEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof MasterBlockEntity) {
				((MasterBlockEntity) blockEntity).setModelInfo(info);
			}
		} else {
			Block block = ForgeRegistries.BLOCKS.getValue(blockTo);
			if (block == null || block == Blocks.AIR)
				return;
			level.setBlockAndUpdate(pos, tryCopyBlockState(state, block.defaultBlockState()));
		}
	}

	@SuppressWarnings("rawtypes")
	public static BlockState tryCopyBlockState(BlockState oldState, BlockState newState) {
		for (Property property : newState.getProperties()) {
			if (oldState.hasProperty(property)) {
				newState.setValue(property, oldState.getValue(property));
			}
		}
		return newState;
	}
}
