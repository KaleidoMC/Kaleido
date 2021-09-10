package snownee.kaleido.resources;

import java.io.File;
import java.io.IOException;

import com.google.gson.JsonObject;

import net.minecraft.resources.FilePack;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.SharedConstants;

public class JarFilePack extends FilePack {

	public JarFilePack(File file) {
		super(file);
	}

	@Override
	public <T> T getMetadataSection(IMetadataSectionSerializer<T> serializer) throws IOException {
		if (hasResource("pack.mcmeta")) {
			return super.getMetadataSection(serializer);
		} else {
			JsonObject jsonobject = new JsonObject();
			if ("pack".equals(serializer.getMetadataSectionName())) {
				int version = SharedConstants.getCurrentVersion().getPackVersion();
				if (hasResource("mcmod.info"))
					version = 3;
				jsonobject.addProperty("pack_format", version);
				jsonobject.addProperty("description", "Jar resources loaded by Kaleido");
			}
			return serializer.fromJson(jsonobject);
		}
	}

}
