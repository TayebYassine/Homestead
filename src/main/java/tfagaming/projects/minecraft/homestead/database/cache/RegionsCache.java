package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.structure.Region;

import java.util.UUID;

public final class RegionsCache extends AbstractCache<Region> {
	@Override
	protected UUID getId(Region region) {
		return region.getUniqueId();
	}
}
