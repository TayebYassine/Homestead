package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.RegionBannedPlayer;

public final class RegionBannedPlayerCache extends AbstractCache<RegionBannedPlayer> {
	@Override
	protected long getId(RegionBannedPlayer model) {
		return model.getUniqueId();
	}
}
