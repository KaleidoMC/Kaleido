package snownee.kaleido.mixin;

import java.util.List;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.DebugChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructureManager;
import snownee.kaleido.KaleidoCommonConfig;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.util.KaleidoTemplate;

@Mixin(DebugChunkGenerator.class)
public class MixinDebugChunkGenerator {

	@Shadow
	protected static BlockState BARRIER;

	private static final List<ModelInfo> ALL_INFOS = Lists.newArrayList();
	private static int GRID_WIDTH;

	@Inject(at = @At("TAIL"), method = "applyBiomeDecoration")
	private void kaleido_applyBiomeDecoration(WorldGenRegion level, StructureManager structureManager, CallbackInfo ci) {
		if (!KaleidoCommonConfig.patchDebugWorld)
			return;
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
		int i = level.getCenterX();
		int j = level.getCenterZ();

		for (int k = 0; k < 16; ++k) {
			for (int l = 0; l < 16; ++l) {
				int i1 = (i << 4) + k;
				int j1 = (j << 4) + l;
				ModelInfo info = getInfoFor(i1, j1);
				if (info != null) {
					blockpos$mutable.set(i1, 70, j1);
					TileEntity blockEntity = level.getBlockEntity(blockpos$mutable);
					if (blockEntity instanceof MasterBlockEntity) {
						((MasterBlockEntity) blockEntity).setModelInfo(info);
					}
				}
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "getBlockStateFor", cancellable = true)
	private static void kaleido_getBlockStateFor(int x, int z, CallbackInfoReturnable<BlockState> ci) {
		if (!KaleidoCommonConfig.patchDebugWorld)
			return;
		ModelInfo info = getInfoFor(x, z);
		if (info != null) {
			ci.setReturnValue(info.template.bloc.defaultBlockState());
		}
	}

	private static ModelInfo getInfoFor(int x, int z) {
		if (ALL_INFOS.isEmpty() || ALL_INFOS.get(0).expired) {
			ALL_INFOS.clear();
			KaleidoDataManager.INSTANCE.allPacks.values().stream().flatMap($ -> Stream.concat($.normalInfos.stream(), $.rewardInfos.stream())).filter($ -> $.template != KaleidoTemplate.item).forEach(ALL_INFOS::add);
			GRID_WIDTH = MathHelper.ceil(ALL_INFOS.size() / 32F);
		}
		int spacing = KaleidoCommonConfig.debugWorldSpacing;
		x = -x;
		z = -z;
		if (x > 0 && z > 0 && x % spacing == spacing - 1 && z % spacing == spacing - 1) {
			x = x / spacing;
			z = z / spacing;
			int GRID_HEIGHT = 32;
			if (x <= GRID_HEIGHT && z <= GRID_WIDTH) {
				int i = MathHelper.abs(x * GRID_WIDTH + z);
				if (i < ALL_INFOS.size()) {
					return ALL_INFOS.get(i);
				}
			}
		}
		return null;
	}
}
