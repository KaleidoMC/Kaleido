package snownee.kaleido.core.action;

import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import snownee.kaleido.core.ModelInfo;

public class CommandAction implements Consumer<ActionContext> {

	private String command;

	public CommandAction(JsonObject obj) {
		this(JSONUtils.getAsString(obj, "command"));
	}

	public CommandAction(String command) {
		this.command = command;
	}

	@Override
	public void accept(ActionContext ctx) {
		World level = ctx.getLevel();
		if (!(level instanceof ServerWorld))
			return;
		MinecraftServer server = level.getServer();
		PlayerEntity player = ctx.getPlayer();
		ModelInfo info = ctx.getModelInfo();
		CommandSource source = new CommandSource(ICommandSource.NULL, Vector3d.atCenterOf(ctx.getBlockPos()), player.getRotationVector(), (ServerWorld) level, 2, info.id.toString(), info.getDescription(), server, player);
		source = source.withSuppressedOutput();
		server.getCommands().performCommand(source, command); //TODO return value?
	}

}
