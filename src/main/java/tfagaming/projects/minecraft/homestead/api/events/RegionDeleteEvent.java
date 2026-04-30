package tfagaming.projects.minecraft.homestead.api.events;

import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionDeleteEvent extends APIEvent {
	private final Region region;

	public RegionDeleteEvent(@NotNull Region region) {
		this.region = region;
	}

	public @NotNull Region getRegion() {
		return region;
	}
}
