package snownee.kaleido.scope;

import org.apache.logging.log4j.util.TriConsumer;

import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.kaleido.scope.block.ScopeBlockEntity;
import snownee.kiwi.item.ModItem;

public class TunerItem extends ModItem {

	private final TriConsumer<ScopeStack, Axis, Float> consumer;
	private final float step;

	public TunerItem(Properties builder, TriConsumer<ScopeStack, Axis, Float> consumer, float step) {
		super(builder);
		this.consumer = consumer;
		this.step = step;
	}

	@Override
	public ActionResultType useOn(ItemUseContext pContext) {
		World level = pContext.getLevel();
		BlockPos pos = pContext.getClickedPos();
		TileEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof ScopeBlockEntity)) {
			return ActionResultType.PASS;
		}
		ScopeBlockEntity scope = (ScopeBlockEntity) blockEntity;
		int i = pContext.getItemInHand().getCount() - 1;
		if (i >= scope.stacks.size()) {
			return ActionResultType.PASS;
		}
		ScopeStack stack = scope.stacks.get(i);
		float f = pContext.getClickedFace().getAxisDirection().getStep() * step;
		if (pContext.getHand() == Hand.OFF_HAND) {
			f = -f;
		}
		consumer.accept(stack, pContext.getClickedFace().getAxis(), f);
		scope.refresh();
		return ActionResultType.sidedSuccess(level.isClientSide);
	}

}
