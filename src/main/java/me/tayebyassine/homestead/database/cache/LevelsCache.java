package me.tayebyassine.homestead.database.cache;

import me.tayebyassine.homestead.models.Level;

public final class LevelsCache extends AbstractCache<Level> {
	@Override
	protected long getId(Level model) {
		return model.getUniqueId();
	}
}