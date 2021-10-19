package snownee.kaleido.core.client.gui;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;

@OnlyIn(Dist.CLIENT)
public abstract class ResizeableScreen extends Screen {

	protected ResizeableScreen(ITextComponent pTitle) {
		super(pTitle);
	}

	@Override
	public void init(Minecraft pMinecraft, int pWidth, int pHeight) {
		minecraft = pMinecraft;
		itemRenderer = pMinecraft.getItemRenderer();
		font = pMinecraft.font;
		width = pWidth;
		height = pHeight;
		Consumer<Widget> remove = b -> {
			buttons.remove(b);
			children.remove(b);
		};
		if (!MinecraftForge.EVENT_BUS.post(new InitGuiEvent.Pre(this, buttons, this::addButton, remove))) {
			buttons.clear();
			children.clear();
			setFocused((IGuiEventListener) null);
			init();
			resize(pMinecraft, pWidth, pHeight);
		}
		MinecraftForge.EVENT_BUS.post(new InitGuiEvent.Post(this, buttons, this::addButton, remove));
	}

	@Override
	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		minecraft = pMinecraft;
		itemRenderer = pMinecraft.getItemRenderer();
		font = pMinecraft.font;
		width = pWidth;
		height = pHeight;
	}

}
