package snownee.kaleido.core.data;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.tileentity.TileEntity;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kiwi.util.NBTHelper;

public class KaleidoLootFunction extends LootFunction {

	private KaleidoLootFunction(ILootCondition[] conditions) {
		super(conditions);
	}

	@Override
	public LootFunctionType getType() {
		return CoreModule.LOOT_FUNCTION_TYPE;
	}

	@Override
	public Set<LootParameter<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootParameters.BLOCK_ENTITY);
	}

	@Override
	public ItemStack run(ItemStack stack, LootContext ctx) {
		TileEntity blockEntity = ctx.getParamOrNull(LootParameters.BLOCK_ENTITY);
		if (blockEntity instanceof MasterBlockEntity) {
			ModelInfo info = ((MasterBlockEntity) blockEntity).getModelInfo();
			if (info != null) {
				if (info.nbt != null) {
					stack.getOrCreateTag().merge(info.nbt);
				}
				NBTHelper data = NBTHelper.of(stack);
				data.setString("Kaleido.Id", info.id.toString());
			}
		}
		return stack;
	}

	public static class Serializer extends LootFunction.Serializer<KaleidoLootFunction> {
		@Override
		public void serialize(JsonObject json, KaleidoLootFunction lootFunction, JsonSerializationContext ctx) {
			super.serialize(json, lootFunction, ctx);
		}

		@Override
		public KaleidoLootFunction deserialize(JsonObject json, JsonDeserializationContext ctx, ILootCondition[] conditions) {
			return new KaleidoLootFunction(conditions);
		}
	}

	public static LootFunction.Builder<?> create() {
		return simpleBuilder(KaleidoLootFunction::new);
	}

}
