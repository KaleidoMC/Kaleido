package snownee.kaleido.core.definition;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

public interface BlockDefinition {

	Map<String, Factory<?>> MAP = Maps.newConcurrentMap();
	List<Factory<?>> FACTORIES = Lists.newLinkedList();

	static void registerFactory(Factory<?> factory) {
		MAP.put(factory.getId(), factory);
		if (factory.getId().equals(SimpleBlockDefinition.TYPE)) {
			FACTORIES.add(factory);
		} else {
			FACTORIES.add(0, factory);
		}
	}

	static BlockDefinition fromNBT(CompoundNBT tag) {
		if (tag == null || !tag.contains("Type")) {
			return null;
		}
		Factory<?> factory = MAP.get(tag.getString("Type"));
		if (factory == null)
			return null;
		return factory.fromNBT(tag);
	}

	static BlockDefinition fromBlock(BlockState state, TileEntity blockEntity, IWorldReader level, BlockPos pos) {
		for (Factory<?> factory : FACTORIES) {
			BlockDefinition supplier = factory.fromBlock(state, blockEntity, level, pos);
			if (supplier != null) {
				return supplier;
			}
		}
		return null;
	}

	static BlockDefinition fromItem(ItemStack stack, BlockItemUseContext context) {
		if (!stack.isEmpty()) {
			for (Factory<?> factory : FACTORIES) {
				BlockDefinition supplier = factory.fromItem(stack, context);
				if (supplier != null) {
					return supplier;
				}
			}
		}
		return null;
	}

	Factory<?> getFactory();

	@OnlyIn(Dist.CLIENT)
	IBakedModel model();

	@OnlyIn(Dist.CLIENT)
	default IModelData modelData() {
		return EmptyModelData.INSTANCE;
	}

	@OnlyIn(Dist.CLIENT)
	RenderMaterial renderMaterial(@Nullable Direction direction);

	void save(CompoundNBT tag);

	@OnlyIn(Dist.CLIENT)
	boolean canRenderInLayer(RenderType layer);

	boolean canOcclude();

	@OnlyIn(Dist.CLIENT)
	int getColor(BlockState blockState, IBlockDisplayReader level, BlockPos pos, int index);

	ITextComponent getDescription();

	boolean place(World level, BlockPos pos);

	ItemStack createItem(RayTraceResult target, IBlockReader world, @Nullable BlockPos pos, @Nullable PlayerEntity player);

	BlockState getBlockState();

	SoundType getSoundType();

	interface Factory<T extends BlockDefinition> {
		T fromNBT(CompoundNBT tag);

		String getId();

		@Nullable
		T fromBlock(BlockState state, TileEntity blockEntity, IWorldReader level, BlockPos pos);

		@Nullable
		T fromItem(ItemStack stack, BlockItemUseContext context);
	}

}
