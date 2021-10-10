package snownee.kaleido.core.supplier;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ModelSupplier {

	Map<String, Function<CompoundNBT, ModelSupplier>> FACTORIES = Maps.newConcurrentMap();

	String getType();

	@OnlyIn(Dist.CLIENT)
	IBakedModel model();

	@OnlyIn(Dist.CLIENT)
	RenderMaterial renderMaterial(Direction direction);

	void save(CompoundNBT tag);

	static ModelSupplier fromNBT(CompoundNBT tag) {
		Function<CompoundNBT, ModelSupplier> function = FACTORIES.get(tag.getString("Type"));
		if (function == null)
			return null;
		return function.apply(tag);
	}

	@OnlyIn(Dist.CLIENT)
	boolean canRenderInLayer(RenderType layer);

	boolean canOcclude();

	@OnlyIn(Dist.CLIENT)
	int getColor(BlockState blockState, IBlockDisplayReader level, BlockPos pos, int index);

	ITextComponent getDescription();

	void place(World level, BlockPos pos);

	BlockState getBlockState();

	SoundType getSoundType();

}
