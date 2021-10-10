package snownee.kaleido.core.supplier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import snownee.kaleido.Hooks;

public class BlockStateModelSupplier implements ModelSupplier {

	public enum Factory implements ModelSupplier.Factory<BlockStateModelSupplier> {
		INSTANCE;

		@SuppressWarnings("deprecation")
		@Override
		public BlockStateModelSupplier fromNBT(CompoundNBT tag) {
			BlockState state = NBTUtil.readBlockState(tag.getCompound(TYPE));
			if (state.isAir())
				return null;
			return of(state);
		}

		@Override
		public BlockStateModelSupplier fromBlock(BlockState state, IWorldReader level, BlockPos pos) {
			return of(state);
		}

		@Override
		public BlockStateModelSupplier fromItem(ItemStack stack, BlockItemUseContext context) {
			if (!(stack.getItem() instanceof BlockItem)) {
				return null;
			}
			BlockItem blockItem = (BlockItem) stack.getItem();
			context = blockItem.updatePlacementContext(context);
			if (context == null) {
				return null;
			}
			BlockState state = Hooks.getStateForPlacement(blockItem, context);
			if (state == null) {
				return null;
			}
			return of(state);
		}

		@Override
		public String getId() {
			return TYPE;
		}

	}

	public static final String TYPE = "Block";
	private static final Map<BlockState, BlockStateModelSupplier> MAP = Maps.newIdentityHashMap();

	public static BlockStateModelSupplier of(BlockState state) {
		if (state.getBlock() == Blocks.GRASS_BLOCK) {
			state = state.setValue(BlockStateProperties.SNOWY, false);
		}
		return MAP.computeIfAbsent(state, BlockStateModelSupplier::new);
	}

	public final BlockState state;
	@OnlyIn(Dist.CLIENT)
	private final RenderMaterial[] materials = new RenderMaterial[7];

	private BlockStateModelSupplier(BlockState state) {
		this.state = state;
	}

	@Override
	public ModelSupplier.Factory<?> getFactory() {
		return Factory.INSTANCE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IBakedModel model() {
		return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public RenderMaterial renderMaterial(Direction direction) {
		int index = direction == null ? 0 : direction.ordinal() + 1;
		if (materials[index] == null) {
			IBakedModel model = model();
			Random random = new Random();
			random.setSeed(42L);
			ResourceLocation particleIcon = model.getParticleTexture(EmptyModelData.INSTANCE).getName();
			ResourceLocation sprite = particleIcon;
			if (state.getBlock() == Blocks.GRASS_BLOCK) {
				direction = Direction.UP;
			}
			if (direction != null) {
				List<BakedQuad> quads = model.getQuads(state, direction, random, EmptyModelData.INSTANCE);
				if (quads.isEmpty())
					quads = model.getQuads(state, null, random, EmptyModelData.INSTANCE);
				for (BakedQuad quad : quads) {
					sprite = quad.getSprite().getName();
					if (sprite.equals(particleIcon)) {
						break;
					}
				}
			}
			materials[index] = ModelLoaderRegistry.blockMaterial(sprite);
		}
		return materials[index];
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderInLayer(RenderType layer) {
		return RenderTypeLookup.canRenderInLayer(state, layer);
	}

	@Override
	public boolean canOcclude() {
		return state.canOcclude();
	}

	@Override
	public void save(CompoundNBT tag) {
		tag.put(TYPE, NBTUtil.writeBlockState(state));
	}

	@Override
	public String toString() {
		return state.toString();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getColor(BlockState blockState, IBlockDisplayReader level, BlockPos worldPosition, int index) {
		return Minecraft.getInstance().getBlockColors().getColor(state, level, worldPosition, index);
	}

	@Override
	public ITextComponent getDescription() {
		return state.getBlock().getName();
	}

	@Override
	public void place(World level, BlockPos pos) {
		BlockState state = this.state;
		if (state.hasProperty(BlockStateProperties.LIT))
			state = state.setValue(BlockStateProperties.LIT, false);
		level.setBlockAndUpdate(pos, state);
	}

	@Override
	public BlockState getBlockState() {
		return state;
	}

	@Override
	public SoundType getSoundType() {
		return state.getSoundType();
	}

	public static void reload() {
		for (BlockStateModelSupplier supplier : MAP.values()) {
			Arrays.fill(supplier.materials, null);
		}
	}

}
