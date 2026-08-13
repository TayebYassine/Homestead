package me.tayebyassine.homestead.database.cache;

import me.tayebyassine.homestead.models.RegionLog;

public final class RegionLogCache extends AbstractCache<RegionLog> {
	@Override
	protected long getId(RegionLog model) {
		return model.getUniqueId();
	}
}
