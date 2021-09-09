package snownee.kaleido.resources;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;

// FolderPackFinder
public class JarPackFinder implements IPackFinder {
	private static final FileFilter RESOURCEPACK_FILTER = file -> {
		return file.isFile() && file.getName().endsWith(".jar");
	};

	private final File folder;
	private final IPackNameDecorator packSource;

	public JarPackFinder(File folder, IPackNameDecorator packSource) {
		this.folder = folder;
		this.packSource = packSource;
	}

	@Override
	public void loadPacks(Consumer<ResourcePackInfo> p_230230_1_, ResourcePackInfo.IFactory p_230230_2_) {
		if (!this.folder.isDirectory()) {
			this.folder.mkdirs();
		}

		File[] afile = this.folder.listFiles(RESOURCEPACK_FILTER);
		if (afile != null) {
			for (File file1 : afile) {
				String s = "file/" + file1.getName();
				ResourcePackInfo resourcepackinfo = ResourcePackInfo.create(s, false, this.createSupplier(file1), p_230230_2_, ResourcePackInfo.Priority.TOP, this.packSource);
				if (resourcepackinfo != null) {
					p_230230_1_.accept(resourcepackinfo);
				}
			}
		}
	}

	private Supplier<IResourcePack> createSupplier(File file) {
		return () -> new JarFilePack(file);
	}
}
