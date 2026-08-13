package me.tayebyassine.homestead.database.cache;

import me.tayebyassine.homestead.models.RegionChunk;

public final class  RegionChunkCache extends AbstractCache<RegionChunk> {
	@Override
	protected long getId(RegionChunk model) {
		return model.getUniqueId();
	}
}
