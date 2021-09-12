package snownee.kaleido.core;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;

public class ModelGroup {

	public final ResourceLocation id;
	public final List<ModelInfo> infos = Lists.newArrayList();

	public ModelGroup(ResourceLocation id) {
		this.id = id;
	}
}
