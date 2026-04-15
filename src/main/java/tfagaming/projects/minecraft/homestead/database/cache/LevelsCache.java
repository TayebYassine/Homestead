package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.structure.Level;

import java.util.UUID;

public final class LevelsCache extends AbstractCache<Level> {
	@Override
	protected UUID getId(Level level) {
		return level.getUniqueId();
	}
}