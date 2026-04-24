package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.RegionMember;

public final class RegionMemberCache extends AbstractCache<RegionMember> {
	@Override
	protected long getId(RegionMember model) {
		return model.getUniqueId();
	}
}
