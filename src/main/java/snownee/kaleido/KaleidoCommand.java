package snownee.kaleido;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.kaleido.hub.HubModule;

@EventBusSubscriber
public final class KaleidoCommand {

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal(Kaleido.MODID);
		/* off */
		if (Hooks.hubEnabled) {
			builder.then(Commands
					.literal("install")
					.then(Commands
							.argument("url", StringArgumentType.string())
			                .requires(ctx -> ctx.hasPermission(2))
			                .executes(ctx -> install(ctx))
					)
			);
		}
		/* on */
		event.getDispatcher().register(builder);
	}

	private static int install(CommandContext<CommandSource> ctx) {
		String url = StringArgumentType.getString(ctx, "url");
		try {
			HubModule.fetch(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
