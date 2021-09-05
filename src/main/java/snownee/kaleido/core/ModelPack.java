package snownee.kaleido.core;

import java.util.List;

import com.google.common.collect.Lists;

public class ModelPack {

	public final List<ModelInfo> normalInfos = Lists.newArrayList();
	public final List<ModelInfo> rewardInfos = Lists.newArrayList();
	public String descriptionId;

	public void add(ModelInfo info) {
		if (info.reward) {
			rewardInfos.add(info);
		} else {
			normalInfos.add(info);
		}
		if (descriptionId == null) {
			descriptionId = "kaleido.pack." + info.id.getNamespace();
		}
	}

}
