package snownee.kaleido.core;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class ModelPack {

	public final List<ModelInfo> normalInfos = Lists.newArrayList();
	public final List<ModelInfo> rewardInfos = Lists.newArrayList();
	public final String id;
	public String descriptionId;

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

	public void sort() {
		Collections.sort(normalInfos);
		Collections.sort(rewardInfos);
	}

}
