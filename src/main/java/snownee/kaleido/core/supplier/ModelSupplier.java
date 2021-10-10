package snownee.kaleido.core.supplier;

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
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ModelSupplier {

	Map<String, Factory<?>> MAP = Maps.newConcurrentMap();
	List<Factory<?>> FACTORIES = Lists.newLinkedList();

	static void registerFactory(Factory<?> factory) {
		MAP.put(factory.getId(), factory);
		if (factory.getId().equals(BlockStateModelSupplier.TYPE)) {
			FACTORIES.add(factory);
		} else {
			FACTORIES.add(0, factory);
		}
	}

	static ModelSupplier fromNBT(CompoundNBT tag) {
		Factory<?> factory = MAP.get(tag.getString("Type"));
		if (factory == null)
			return null;
		return factory.fromNBT(tag);
	}

	static ModelSupplier fromBlock(BlockState state, IWorldReader level, BlockPos pos) {
		for (Factory<?> factory : FACTORIES) {
			ModelSupplier supplier = factory.fromBlock(state, level, pos);
			if (supplier != null) {
				return supplier;
			}
		}
		return null;
	}

	static ModelSupplier fromItem(ItemStack stack, BlockItemUseContext context) {
		for (Factory<?> factory : FACTORIES) {
			ModelSupplier supplier = factory.fromItem(stack, context);
			if (supplier != null) {
				return supplier;
			}
		}
		return null;
	}

	Factory<?> getFactory();

	@OnlyIn(Dist.CLIENT)
	IBakedModel model();

	@OnlyIn(Dist.CLIENT)
	RenderMaterial renderMaterial(Direction direction);

	void save(CompoundNBT tag);

	@OnlyIn(Dist.CLIENT)
	boolean canRenderInLayer(RenderType layer);

	boolean canOcclude();

	@OnlyIn(Dist.CLIENT)
	int getColor(BlockState blockState, IBlockDisplayReader level, BlockPos pos, int index);

	ITextComponent getDescription();

	void place(World level, BlockPos pos);

	BlockState getBlockState();

	SoundType getSoundType();

	interface Factory<T extends ModelSupplier> {
		T fromNBT(CompoundNBT tag);

		String getId();

		@Nullable
		T fromBlock(BlockState state, IWorldReader level, BlockPos pos);

		@Nullable
		T fromItem(ItemStack stack, BlockItemUseContext context);
	}

}
