package me.tayebyassine.homestead.database.cache;

import me.tayebyassine.homestead.models.RegionRate;

public final class RegionRateCache extends AbstractCache<RegionRate> {
	@Override
	protected long getId(RegionRate model) {
		return model.getUniqueId();
	}
}
