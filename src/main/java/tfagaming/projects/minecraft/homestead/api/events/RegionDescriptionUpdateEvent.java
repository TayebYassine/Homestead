package tfagaming.projects.minecraft.homestead.api.events;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionDescriptionUpdateEvent extends APIEvent {
	private final Region region;
	private final String oldDescription;
	private final String newDescription;

	public RegionDescriptionUpdateEvent(@NotNull Region region, @Nullable String oldDescription, @Nullable String newDescription) {
		this.region = region;
		this.oldDescription = oldDescription;
		this.newDescription = newDescription;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @Nullable String getOldDescription() {
		return oldDescription;
	}

	public @Nullable String getNewDescription() {
		return newDescription;
	}
}
