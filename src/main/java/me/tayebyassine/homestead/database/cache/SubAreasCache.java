package me.tayebyassine.homestead.database.cache;

import me.tayebyassine.homestead.models.SubArea;

public final class SubAreasCache extends AbstractCache<SubArea> {
	@Override
	protected long getId(SubArea model) {
		return model.getUniqueId();
	}
}
