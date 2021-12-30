package snownee.kaleido.core;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModelPack {

	public final List<ModelInfo> normalInfos = Lists.newArrayList();
	public final List<ModelInfo> rewardInfos = Lists.newArrayList();
	public final String id;
	public String descriptionId;
	public ItemGroup tab;

	public ModelPack(String id) {
		this.id = id;
		descriptionId = "kaleido.pack." + id;
	}

	public void add(ModelInfo info) {
		if (info.reward) {
			rewardInfos.add(info);
		} else {
			normalInfos.add(info);
		}
	}

	@SuppressWarnings("deprecation")
	public void sort() {
		Collections.sort(normalInfos);
		Collections.sort(rewardInfos);
		tab = new ItemGroup(descriptionId) {
			private final ITextComponent displayName = new TranslationTextComponent(descriptionId);

			@Override
			public ItemStack makeIcon() {
				if (!normalInfos.isEmpty()) {
					return normalInfos.get(0).makeItem();
				}
				if (!rewardInfos.isEmpty()) {
					return rewardInfos.get(0).makeItem();
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
				Stream.concat(normalInfos.stream(), rewardInfos.stream()).map(ModelInfo::makeItem).forEach(pItems::add);
			}
		}.setBackgroundSuffix("item_search.png");
	}

}
