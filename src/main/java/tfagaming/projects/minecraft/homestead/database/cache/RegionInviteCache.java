package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.RegionInvite;

public final class RegionInviteCache extends AbstractCache<RegionInvite> {
	@Override
	protected long getId(RegionInvite model) {
		return model.getUniqueId();
	}
}
