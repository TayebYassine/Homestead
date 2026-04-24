package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.RegionRate;

public final class RegionRateCache extends AbstractCache<RegionRate> {
	@Override
	protected long getId(RegionRate model) {
		return model.getUniqueId();
	}
}
