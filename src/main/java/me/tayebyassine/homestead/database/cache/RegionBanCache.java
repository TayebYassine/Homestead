package me.tayebyassine.homestead.database.cache;

import me.tayebyassine.homestead.models.RegionBan;

public final class RegionBanCache extends AbstractCache<RegionBan> {
	@Override
	protected long getId(RegionBan model) {
		return model.getUniqueId();
	}
}
