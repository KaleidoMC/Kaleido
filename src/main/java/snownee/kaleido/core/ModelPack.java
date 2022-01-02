package snownee.kaleido.core;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import snownee.kiwi.util.Util;

public class ModelPack {

	public final List<ModelInfo> normalInfos = Lists.newArrayList();
	public final List<ModelInfo> rewardInfos = Lists.newArrayList();
	public final String id;
	public List<String> author;
	public ResourceLocation icon;
	public String descriptionId;
	public ItemGroup tab;

	public ModelPack(String id) {
		this.id = id;
		descriptionId = "kaleido.pack." + id;
	}

	public void fromJson(JsonObject object) {
		author = KaleidoDataManager.GSON.fromJson(object.get("author"), List.class);
		icon = Util.RL(object.get("icon").getAsString());
	}

	public void fromNetwork(PacketBuffer buf) {
		int n = buf.readVarInt();
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (int i = 0; i < n; i++) {
			builder.add(buf.readUtf());
		}
		author = builder.build();
		String icon = buf.readUtf();
		if (!icon.isEmpty()) {
			this.icon = Util.RL(icon);
		}
	}

	public void toNetwork(PacketBuffer buf) {
		buf.writeUtf(id);
		int n = author == null ? 0 : author.size();
		buf.writeVarInt(n);
		if (n != 0) {
			for (String name : author) {
				buf.writeUtf(name);
			}
		}
		if (icon == null) {
			buf.writeUtf("");
		} else {
			buf.writeUtf(icon.toString());
		}
	}

	public void add(ModelInfo info) {
		if (info.reward) {
			rewardInfos.add(info);
		} else {
			normalInfos.add(info);
		}
	}

	public void sort() {
		Collections.sort(normalInfos);
		Collections.sort(rewardInfos);
		tab = new KaleidoCreativeTab(this);
	}

}
