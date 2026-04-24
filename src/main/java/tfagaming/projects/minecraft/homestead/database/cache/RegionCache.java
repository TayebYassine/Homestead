package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.Region;

public final class RegionCache extends AbstractCache<Region> {
	@Override
	protected long getId(Region model) {
		return model.getUniqueId();
	}
}
