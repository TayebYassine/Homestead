package me.tayebyassine.homestead.database.cache;

import me.tayebyassine.homestead.models.War;

public final class WarsCache extends AbstractCache<War> {
	@Override
	protected long getId(War model) {
		return model.getUniqueId();
	}
}
