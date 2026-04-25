package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.RegionBan;

public final class RegionBanCache extends AbstractCache<RegionBan> {
	@Override
	protected long getId(RegionBan model) {
		return model.getUniqueId();
	}
}
