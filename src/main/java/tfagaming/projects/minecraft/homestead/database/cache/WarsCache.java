package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.structure.War;

import java.util.UUID;

public final class WarsCache extends AbstractCache<War> {
	@Override
	protected UUID getId(War war) {
		return war.getUniqueId();
	}
}
