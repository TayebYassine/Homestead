package me.tayebyassine.homestead.database.cache;

import me.tayebyassine.homestead.models.RegionMember;

public final class RegionMemberCache extends AbstractCache<RegionMember> {
	@Override
	protected long getId(RegionMember model) {
		return model.getUniqueId();
	}
}
