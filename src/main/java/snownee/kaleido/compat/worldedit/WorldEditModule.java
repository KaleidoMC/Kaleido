package snownee.kaleido.compat.worldedit;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kaleido.Kaleido;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.util.KaleidoTemplate;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;

@KiwiModule(value = "worldedit", dependencies = "worldedit")
@KiwiModule.Subscriber
public class WorldEditModule extends AbstractModule {

	private static boolean worldLoaded;

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		IWorld level = event.getWorld();
		if (!(level instanceof ServerWorld)) {
			return;
		}
		ServerWorld serverWorld = (ServerWorld) level;
		if (serverWorld.dimension() != World.OVERWORLD) {
			return;
		}

		worldLoaded = true;
		if (generateMappings(serverWorld.getServer()))
			runScript(serverWorld);
	}

	public static boolean generateMappings(MinecraftServer server) {
		if (!worldLoaded)
			return false;
		try {
			Path dir = server.getFile("config/worldedit/craftscripts").toPath();
			Path parser = dir.resolve("kaleido-parser.js");
			if (!Files.exists(parser))
				return false;
			Path file = dir.resolve("kaleido-mappings.json");
			Files.deleteIfExists(file);

			JsonObject json = new JsonObject();
			for (ModelInfo info : KaleidoDataManager.INSTANCE.allInfos.values()) {
				if (info.template == KaleidoTemplate.item)
					continue;
				String v;
				if (info.template.bloc == CoreModule.STUFF)
					v = "";
				else
					v = info.template.bloc.getRegistryName().getPath();
				json.addProperty(info.id.toString(), v);
			}
			String s = KaleidoDataManager.GSON.toJson(json);

			try (BufferedWriter bufferedwriter = Files.newBufferedWriter(file)) {
				bufferedwriter.write(s);
			}
			return true;
		} catch (IOException e) {
			Kaleido.logger.catching(e);
			return false;
		}
	}

	public static void runScript(ServerWorld serverWorld) {
		MinecraftServer server = serverWorld.getServer();
		server.getCommands().performCommand(server.createCommandSourceStack(), "cs kaleido-parser");
	}
}
