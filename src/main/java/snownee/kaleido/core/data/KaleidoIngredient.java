package snownee.kaleido.core.data;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import snownee.kaleido.Kaleido;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.KaleidoBlock;

public class KaleidoIngredient extends Ingredient {

	public final ResourceLocation modelId;

	public KaleidoIngredient(ResourceLocation modelId) {
		super(Stream.of(new Ingredient.SingleItemList(ModelInfo.makeItemStack(1, modelId, null))));
		this.modelId = modelId;
	}

	@Override
	public boolean test(@Nullable ItemStack input) {
		if (input == null || input.getItem() != CoreModule.STUFF_ITEM)
			return false;
		ModelInfo info = KaleidoBlock.getInfo(input);
		if (info == null)
			return false;
		return modelId.equals(info.id);
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public Serializer getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", Kaleido.MODID);
		json.addProperty("item", modelId.toString());
		int count = getItems()[0].getCount();
		if (count != 1)
			json.addProperty("count", count);
		return json;
	}

	public enum Serializer implements IIngredientSerializer<KaleidoIngredient> {
		INSTANCE;

		@Override
		public KaleidoIngredient parse(PacketBuffer buffer) {
			return new KaleidoIngredient(buffer.readResourceLocation());
		}

		@Override
		public KaleidoIngredient parse(JsonObject json) {
			return new KaleidoIngredient(new ResourceLocation(JSONUtils.getAsString(json, "item")));
		}

		@Override
		public void write(PacketBuffer buffer, KaleidoIngredient ingredient) {
			buffer.writeResourceLocation(ingredient.modelId);
		}
	}
}
