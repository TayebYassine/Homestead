package tfagaming.projects.minecraft.homestead.api.events;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionDisplaynameUpdateEvent extends APIEvent {
	private final Region region;
	private final String oldDisplayname;
	private final String newDisplayname;

	public RegionDisplaynameUpdateEvent(@NotNull Region region, @Nullable String oldDisplayname, @Nullable String newDisplayname) {
		this.region = region;
		this.oldDisplayname = oldDisplayname;
		this.newDisplayname = newDisplayname;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @Nullable String getOldDisplayname() {
		return oldDisplayname;
	}

	public @Nullable String getNewDisplayname() {
		return newDisplayname;
	}
}
