package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.structure.SubArea;

import java.util.UUID;

public final class SubAreasCache extends AbstractCache<SubArea> {
	@Override
	protected UUID getId(SubArea subArea) {
		return subArea.getUniqueId();
	}
}
