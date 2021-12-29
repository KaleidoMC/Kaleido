package snownee.kaleido.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.world.World;

@Mixin(ChunkRenderCache.class)
public abstract class ChunkRenderCacheAccessor {

	@Accessor
	public abstract World getLevel();

}
