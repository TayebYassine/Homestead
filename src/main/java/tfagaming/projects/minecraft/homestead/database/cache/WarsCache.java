package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.War;

public final class WarsCache extends AbstractCache<War> {
	@Override
	protected long getId(War model) {
		return model.getUniqueId();
	}
}
