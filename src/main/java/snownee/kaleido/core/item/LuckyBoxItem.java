package snownee.kaleido.core.item;

import java.util.Collection;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kiwi.item.ModItem;

public class LuckyBoxItem extends ModItem {

    public LuckyBoxItem(Properties builder) {
        super(builder);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

        ItemStack stack = playerIn.getHeldItem(handIn);
        Collection<ModelInfo> infos = KaleidoDataManager.INSTANCE.allInfos.values();
        if (infos.isEmpty()) {
            return ActionResult.resultFail(stack);
        } else {
            if (!worldIn.isRemote) {
                //            List<ModelInfo> list = ImmutableList.copyOf(infos);
                //            ModelInfo info = list.get(worldIn.rand.nextInt(list.size()));
                //            ItemHandlerHelper.giveItemToPlayer(playerIn, info.makeItem());
                ModelInfo info = KaleidoDataManager.INSTANCE.getRandomUnlocked((ServerPlayerEntity) playerIn, playerIn.getRNG());
                if (info != null && info.grant((ServerPlayerEntity) playerIn)) {
                    if (!playerIn.isCreative()) {
                        stack.shrink(1);
                    }
                    playerIn.sendMessage(new StringTextComponent("Unlock: " + info.id));
                }
            }
            return ActionResult.resultSuccess(stack);
        }
    }

}
