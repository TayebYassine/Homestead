package me.tayebyassine.homestead.database.cache;

import me.tayebyassine.homestead.models.RegionInvite;

public final class RegionInviteCache extends AbstractCache<RegionInvite> {
	@Override
	protected long getId(RegionInvite model) {
		return model.getUniqueId();
	}
}
