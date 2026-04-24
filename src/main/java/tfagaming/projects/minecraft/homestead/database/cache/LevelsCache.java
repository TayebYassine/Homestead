package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.Level;

public final class LevelsCache extends AbstractCache<Level> {
	@Override
	protected long getId(Level model) {
		return model.getUniqueId();
	}
}