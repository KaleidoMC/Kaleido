package snownee.kaleido.scope.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.kaleido.scope.block.ScopeBlockEntity;

public class ScopeScreen extends Screen {

	public ScopeScreen(ScopeBlockEntity blockEntity) {
		super(new TranslationTextComponent("gui.kaleido.scope"));
	}

}
