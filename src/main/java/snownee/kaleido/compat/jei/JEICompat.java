package snownee.kaleido.compat.jei;

import java.util.Collection;
import java.util.stream.Collectors;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import snownee.kaleido.Kaleido;
import snownee.kaleido.KaleidoClientConfig;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.KaleidoBlocks;

@JeiPlugin
public class JEICompat implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation(Kaleido.MODID, "main");

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		if (KaleidoClientConfig.showInJEI) {
			registration.registerSubtypeInterpreter(CoreModule.STUFF_ITEM, (stack, ctx) -> {
				ModelInfo info = KaleidoBlocks.getInfo(stack);
				if (info == null || info.expired)
					return IIngredientSubtypeInterpreter.NONE;
				return info.id.toString();
			});
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		if (!KaleidoClientConfig.showInJEI) {
			IIngredientManager ingredientManager = runtime.getIngredientManager();
			IIngredientType<ItemStack> itemType = ingredientManager.getIngredientType(ItemStack.class);
			/* off */
			Collection<ItemStack> items = ingredientManager.getAllIngredients(itemType)
					.stream()
					.filter(stack -> stack.getItem() == CoreModule.STUFF_ITEM)
					.collect(Collectors.toList());
			/* on */
			runtime.getIngredientManager().removeIngredientsAtRuntime(itemType, items);
		}
	}

}
