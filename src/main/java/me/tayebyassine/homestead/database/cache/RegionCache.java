package me.tayebyassine.homestead.database.cache;

import me.tayebyassine.homestead.models.Region;

public final class RegionCache extends AbstractCache<Region> {
	@Override
	protected long getId(Region model) {
		return model.getUniqueId();
	}
}
