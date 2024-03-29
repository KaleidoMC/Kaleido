package snownee.kaleido.preview;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kaleido.Hooks;
import snownee.kaleido.KaleidoClientConfig;
import snownee.kaleido.chisel.client.model.RetextureModel;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.KaleidoBlock;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.template.KaleidoTemplate;
import snownee.kaleido.scope.ScopeModule;
import snownee.kaleido.util.GhostRenderType;
import snownee.kaleido.util.SimulationBlockReader;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.NBTHelper.NBT;
import team.chisel.ctm.Configurations;

@OnlyIn(Dist.CLIENT)
public final class PlacementPreview {

	static final KeyBinding toggle = new KeyBinding("key.kaleido.togglePreview", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM.getOrCreate(333), "Kaleido");

	@SubscribeEvent
	public static void onKeyPressed(KeyInputEvent event) {
		if (event.getAction() != 1)
			return;

		if (toggle.isDown()) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.screen != null) {
				return;
			}
			KaleidoClientConfig.previewEnabled = !KaleidoClientConfig.previewEnabled;
		}
	}

	private static ItemStack lastStack = ItemStack.EMPTY;

	private static final PreviewTransform transform = new PreviewTransform();
	private static final SimulationBlockReader blockReader = new SimulationBlockReader();

	static {
		blockReader.useSelfLight(true);
	}

	private static boolean successLast;

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		successLast = renderInternal(event);
	}

	@SuppressWarnings("deprecation")
	private static boolean renderInternal(RenderWorldLastEvent event) {
		if (!KaleidoClientConfig.previewEnabled) {
			return false;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.overlay != null || mc.screen != null || mc.player == null || mc.options.hideGui || mc.options.keyAttack.isDown() || !(mc.hitResult instanceof BlockRayTraceResult) || mc.hitResult.getType() == RayTraceResult.Type.MISS/* || mc.level.isDebug()*/) {
			return false;
		}
		if (mc.gameMode.getPlayerMode().isBlockPlacingRestricted())
			return false;

		ClientPlayerEntity player = mc.player;
		if (player.getDeltaMovement().lengthSqr() > 0.01)
			return false;
		ItemStack held = player.getMainHandItem();
		if (!(held.getItem() instanceof BlockItem)) {
			held = player.getOffhandItem();
		}
		if (!KaleidoClientConfig.previewAllBlocks && held.getItem() != CoreModule.STUFF_ITEM) {
			return false;
		}
		if (held.getItem() instanceof BlockItem) {
			BlockItem theBlockItem = (BlockItem) held.getItem();
			ModelInfo info = KaleidoBlock.getInfo(held);
			if (theBlockItem == CoreModule.STUFF_ITEM) {
				if (info == null || info.template == KaleidoTemplate.ITEM) {
					return false;
				}
				if (info.template.allowsCustomShape()) { // do not render if held model is simple and the same as target
					TileEntity tile = mc.level.getBlockEntity(((BlockRayTraceResult) mc.hitResult).getBlockPos());
					if (tile instanceof MasterBlockEntity) {
						MasterBlockEntity masterTile = (MasterBlockEntity) tile;
						if (masterTile.getModelInfo() != null && masterTile.getModelInfo().id.equals(info.id)) {
							return false;
						}
					}
				}
			}
			BlockItemUseContext context = theBlockItem.updatePlacementContext(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, (BlockRayTraceResult) mc.hitResult)));
			if (context == null || !context.canPlace()) {
				return false;
			}
			BlockState placeResult = Hooks.getStateForPlacement(theBlockItem, context);
			if (placeResult == null) {
				return false;
			}

			World world = context.getLevel();
			BlockPos target = ((BlockRayTraceResult) mc.hitResult).getBlockPos();
			if (Hooks.scopeEnabled && world.getBlockState(target).is(ScopeModule.SCOPE)) {
				return false;
			}

			target = context.getClickedPos();
			boolean replace = !world.getBlockState(target).isAir();

			Direction direction = null;
			if (replace) {
				transform.canRotate = false;
			} else if (placeResult.hasProperty(HorizontalBlock.FACING)) {
				direction = placeResult.getValue(HorizontalBlock.FACING);
				placeResult = placeResult.setValue(HorizontalBlock.FACING, Direction.NORTH);
			} else if (placeResult.hasProperty(DirectionalBlock.FACING) && placeResult.getValue(DirectionalBlock.FACING).get2DDataValue() != -1) {
				direction = placeResult.getValue(DirectionalBlock.FACING);
				placeResult = placeResult.setValue(DirectionalBlock.FACING, Direction.NORTH);
			} else {
				transform.canRotate = false;
			}
			if (info != null && info.template == KaleidoTemplate.BLOCK) {
				direction = null;
			}
			if (direction != null) {
				transform.rotate(direction.toYRot() + 180);
			}

			if (lastStack != held) {
				lastStack = held;
				transform.pos(target);
			} else if (replace) {
				transform.pos(target);
			}
			GhostRenderType.Buffer renderBuffer = GhostRenderType.defaultBuffer();
			MatrixStack transforms = event.getMatrixStack();
			Vector3d projVec = mc.getEntityRenderDispatcher().camera.getPosition();
			transforms.translate(-projVec.x, -projVec.y, -projVec.z);
			transforms.pushPose();
			if (successLast) {
				transform.target(target);
			} else {
				transform.pos(target);
			}
			transform.tick(mc.getDeltaFrameTime());
			transforms.translate(transform.getX() + .5, transform.getY() + .5, transform.getZ() + .5);
			if (transform.canRotate)
				transforms.mulPose(transform.getRotation());
			transforms.translate(-.5, -.5, -.5);
			float alpha = KaleidoClientConfig.previewAlpha + MathHelper.sin(Animation.getWorldTime(Minecraft.getInstance().level) * 4) * 0.05F;
			renderBuffer.setAlpha(alpha);
			GhostRenderType.disableDepthTest = true;
			renderBlock(transforms, world, placeResult, target, held, info, renderBuffer);
			if (placeResult.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && placeResult.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
				transforms.translate(0, 1, 0);
				renderBlock(transforms, world, placeResult.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), target.above(), held, info, renderBuffer);
			} else if (placeResult.hasProperty(BlockStateProperties.BED_PART) && placeResult.hasProperty(HorizontalBlock.FACING)) {
				Direction facing = placeResult.getValue(HorizontalBlock.FACING);
				transforms.translate(facing.getStepX(), 0, facing.getStepZ());
				renderBlock(transforms, world, placeResult.setValue(BlockStateProperties.BED_PART, BedPart.HEAD), target.relative(facing), held, info, renderBuffer);
			}
			transforms.popPose();
			renderBuffer.endBatch();
			GhostRenderType.disableDepthTest = false;
		}
		return true;
	}

	private static void renderBlock(MatrixStack transforms, World world, BlockState state, BlockPos pos, ItemStack stack, ModelInfo info, IRenderTypeBuffer renderBuffer) {
		BlockRenderType renderType = state.getRenderShape();
		Minecraft mc = Minecraft.getInstance();

		/* Assume renderType is not null.
         *
         * Yes, we use a fake tile entity to workaround this. All exceptions are
         * discared. It is ugly, yes, but it partially solve the problem.
         */
		TileEntity tile = null;
		if (state.hasTileEntity()) {
			tile = state.createTileEntity(world);
			@SuppressWarnings("rawtypes")
			TileEntityRenderer renderer = TileEntityRendererDispatcher.instance.getRenderer(tile);
			tile.setLevelAndPosition(world, pos);
			tile.blockState = state;
			//			if (tile instanceof BannerTileEntity && state.getBlock() instanceof AbstractBannerBlock) {
			//				((BannerTileEntity) tile).fromItem(stack, ((AbstractBannerBlock) state.getBlock()).getColor());
			//			}
			if (stack.hasTag()) {
				if (info != null && tile instanceof MasterBlockEntity) {
					MasterBlockEntity master = (MasterBlockEntity) tile;
					master.setModelInfo(info);
				} else {
					CompoundNBT tileData = stack.getTagElement("BlockEntityTag");
					if (tileData != null) {
						tile.load(state, tileData);
						tile.setPosition(pos);
					}
				}
			}
			if (renderer != null) {
				try {
					// 0x00F0_00F0 means "full sky light and full block light".
					// Reference: LightTexture.packLight (func_228451_a_)
					renderer.render(tile, 0F, transforms, renderBuffer, 0x00F0_00F0, OverlayTexture.NO_OVERLAY);
				} catch (Throwable ignored) {
				}
			}
		}

		if (renderType == BlockRenderType.MODEL) {
			IModelData data = EmptyModelData.INSTANCE;
			//				if (data == null) {
			//					data = EmptyModelData.INSTANCE;
			//				}
			BlockRendererDispatcher dispatcher = mc.getBlockRenderer();
			IBakedModel bakedModel;
			if (stack.getItem() == CoreModule.STUFF_ITEM) {
				bakedModel = KaleidoClient.getModel(info, state);
			} else {
				bakedModel = dispatcher.getBlockModelShaper().getBlockModel(state);
				if (Hooks.chiselEnabled && NBTHelper.of(stack).hasTag("BlockEntityTag.Overrides", NBT.COMPOUND)) {
					data = new ModelDataMap.Builder().withInitial(RetextureModel.TEXTURES, RetextureModel.OverrideList.overridesFromItem(stack)).build();
				}
			}
			long i = state.getSeed(pos);
			boolean preDisableCTM = false;
			if (KaleidoClient.ctm) {
				preDisableCTM = Configurations.disableCTM;
				Configurations.disableCTM = true;
			}
			transforms.pushPose();
			IBlockDisplayReader displayReader;
			if (tile == null) {
				displayReader = world;
			} else {
				blockReader.setLevel(world);
				blockReader.setPos(tile.getBlockPos());
				blockReader.setBlockEntity(tile);
				displayReader = blockReader;
			}
			dispatcher.getModelRenderer().renderModel(displayReader, bakedModel, state, pos, transforms, renderBuffer.getBuffer(RenderTypeLookup.getRenderType(state, false)), false, new Random(), i, OverlayTexture.NO_OVERLAY, data);
			transforms.popPose();
			if (KaleidoClient.ctm) {
				Configurations.disableCTM = preDisableCTM;
			}
		}

	}
}
