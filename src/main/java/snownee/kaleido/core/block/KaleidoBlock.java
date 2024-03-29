package snownee.kaleido.core.block;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.action.ActionContext;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.template.KaleidoTemplate;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.Util;

public interface KaleidoBlock extends IForgeBlock {

	@Override
	default boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	default TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MasterBlockEntity();
	}

	@Override
	default ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ItemStack stack = new ItemStack(CoreModule.STUFF_ITEM);
		ModelInfo info = getInfo(world, pos);
		if (info != null) {
			NBTHelper.of(stack).setString("Kaleido.Id", info.id.toString());
		}
		return stack;
	}

	@Override
	default int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		ModelInfo info = getInfo(world, pos);
		return info == null ? 0 : info.lightEmission;
	}

	@Override
	default SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
		ModelInfo info = getInfo(world, pos);
		return info == null ? SoundType.WOOD : info.soundType.soundType;
	}

	KaleidoTemplate getTemplate();

	static ModelInfo getInfo(ItemStack stack) {
		if (stack.getItem() != CoreModule.STUFF_ITEM)
			return null;
		NBTHelper data = NBTHelper.of(stack);
		ResourceLocation modelId = Util.RL(data.getString("Kaleido.Id"));
		if (modelId == null || modelId.getPath().isEmpty()) {
			return null;
		}
		return KaleidoDataManager.get(modelId);
	}

	@Nullable
	static ModelInfo getInfo(IBlockReader level, BlockPos pos) {
		ModelInfo info = null;
		if (FMLEnvironment.dist.isClient() && level instanceof ServerWorld && Minecraft.getInstance().level != null) {
			level = Minecraft.getInstance().level;
		}
		if (level instanceof World) {
			GlobalPos globalPos = GlobalPos.of(((World) level).dimension(), pos.immutable());
			info = ModelInfo.cache.getIfPresent(globalPos);
			if (info == null || info.expired) {
				TileEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof MasterBlockEntity) {
					info = ((MasterBlockEntity) blockEntity).getModelInfo();
					if (info != null)
						ModelInfo.cache.put(globalPos, info);
				}
			}
		} else {
			TileEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof MasterBlockEntity) {
				info = ((MasterBlockEntity) blockEntity).getModelInfo();
			}
		}
		return info;
	}

	static void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (stack.getItem() != CoreModule.STUFF_ITEM)
			return;
		ModelInfo info = getInfo(stack);
		if (info == null) {
			worldIn.destroyBlock(pos, true);
			return;
		}
		TileEntity tile = worldIn.getBlockEntity(pos);
		if (tile instanceof MasterBlockEntity) {
			((MasterBlockEntity) tile).setModelInfo(info);
		}
	}

	static ActionResultType use(BlockState state, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand handIn, BlockRayTraceResult hit) {
		if (handIn == Hand.OFF_HAND)
			return ActionResultType.PASS;
		TileEntity tile = pLevel.getBlockEntity(pPos);
		if (tile instanceof MasterBlockEntity)
			return ((MasterBlockEntity) tile).use(state, pPlayer, handIn, hit);
		return ActionResultType.PASS;
	}

	static void attack(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
		ModelInfo info = getInfo(pLevel, pPos);
		if (info != null) {
			ActionContext ctx = new ActionContext(info, pPlayer, pPos);
			info.fireEvent("event.attackBlock", ctx);
		}
	}

	static void onProjectileHit(World pLevel, BlockState pState, BlockRayTraceResult pHit, ProjectileEntity pProjectile) {
		ModelInfo info = getInfo(pLevel, pHit.getBlockPos());
		if (info != null) {
			ActionContext ctx = new ActionContext(info, pLevel, null);
			ctx.hitResult = pHit;
			ctx.entity = pProjectile;
			info.fireEvent("event.projectileHit", ctx);
		}
	}

	static VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		KaleidoTemplate template = ((KaleidoBlock) state.getBlock()).getTemplate();
		Preconditions.checkArgument(template.allowsCustomShape());
		ModelInfo info = getInfo(worldIn, pos);
		if (info != null) {
			VoxelShape shape = info.getShape(worldIn, state, pos);
			if (!shape.isEmpty()) {
				return shape;
			}
		}
		return VoxelShapes.block();
	}

	static VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		KaleidoTemplate template = ((KaleidoBlock) state.getBlock()).getTemplate();
		Preconditions.checkArgument(template.allowsCustomShape());
		ModelInfo info = getInfo(worldIn, pos);
		if (info != null && !info.noCollision) {
			VoxelShape shape = info.getShape(worldIn, state, pos);
			if (info.outOfBlock()) {
				shape = VoxelShapes.join(shape, VoxelShapes.block(), IBooleanFunction.AND);
			}
			return shape;
		}
		return VoxelShapes.empty();
	}

	static List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
		ResourceLocation resourcelocation = pState.getBlock().getLootTable();
		TileEntity blockEntity = pBuilder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
		if (blockEntity instanceof MasterBlockEntity) {
			ModelInfo info = ((MasterBlockEntity) blockEntity).getModelInfo();
			if (info != null && info.lootTable != null) {
				resourcelocation = info.lootTable;
			}
		}
		if (resourcelocation == LootTables.EMPTY) {
			return Collections.emptyList();
		} else {
			LootContext lootcontext = pBuilder.withParameter(LootParameters.BLOCK_STATE, pState).create(LootParameterSets.BLOCK);
			ServerWorld serverworld = lootcontext.getLevel();
			LootTable loottable = serverworld.getServer().getLootTables().get(resourcelocation);
			return loottable.getRandomItems(lootcontext);
		}
	}

	@Override
	default boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return false;
	}

}
