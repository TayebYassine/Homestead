package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.SubArea;

public final class SubAreasCache extends AbstractCache<SubArea> {
	@Override
	protected long getId(SubArea model) {
		return model.getUniqueId();
	}
}
