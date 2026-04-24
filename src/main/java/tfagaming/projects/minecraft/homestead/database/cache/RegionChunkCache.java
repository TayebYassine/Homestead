package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.RegionChunk;

public final class RegionChunkCache extends AbstractCache<RegionChunk> {
	@Override
	protected long getId(RegionChunk model) {
		return model.getUniqueId();
	}
}
