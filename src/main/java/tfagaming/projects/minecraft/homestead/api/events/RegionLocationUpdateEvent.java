package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionLocationUpdateEvent extends APIEvent {
	private final Region region;
	private final Location oldLocation;
	private final Location newLocation;

	public RegionLocationUpdateEvent(@NotNull Region region, @Nullable Location oldLocation, @Nullable Location newLocation) {
		this.region = region;
		this.oldLocation = oldLocation;
		this.newLocation = newLocation;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @Nullable Location getOldLocation() {
		return oldLocation;
	}

	public @Nullable Location getNewLocation() {
		return newLocation;
	}
}
