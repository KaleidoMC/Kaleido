package snownee.kaleido.core;

import java.util.stream.Stream;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KaleidoCreativeTab extends ItemGroup {

	private final ModelPack pack;
	private final ITextComponent displayName;

	@SuppressWarnings("deprecation")
	public KaleidoCreativeTab(ModelPack pack) {
		super(pack.descriptionId);
		this.pack = pack;
		displayName = new TranslationTextComponent(pack.descriptionId);
		setBackgroundSuffix("item_search.png");
	}

	@Override
	public ItemStack makeIcon() {
		if (pack.icon != null && KaleidoDataManager.get(pack.icon) != null) {
			return KaleidoDataManager.get(pack.icon).makeItem();
		}
		if (!pack.normalInfos.isEmpty()) {
			return pack.normalInfos.get(0).makeItem();
		}
		if (!pack.rewardInfos.isEmpty()) {
			return pack.rewardInfos.get(0).makeItem();
		}
		return new ItemStack(Items.BRICK);
	}

	@Override
	public ITextComponent getDisplayName() {
		return displayName;
	}

	@Override
	public boolean hasSearchBar() {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillItemList(NonNullList<ItemStack> pItems) {
		Stream.concat(pack.normalInfos.stream(), pack.rewardInfos.stream()).map(ModelInfo::makeItem).forEach(pItems::add);
	}

}
