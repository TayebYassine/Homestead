package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.RegionLog;

public final class RegionLogCache extends AbstractCache<RegionLog> {
	@Override
	protected long getId(RegionLog model) {
		return model.getUniqueId();
	}
}
