package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionLocationUpdateEvent extends APIEvent {
	private final Region region;
	private final Location oldLocation;
	private final Location newLocation;

	public RegionLocationUpdateEvent(@NotNull Region region, @NotNull Location oldLocation, @NotNull Location newLocation) {
		this.region = region;
		this.oldLocation = oldLocation;
		this.newLocation = newLocation;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull Location getOldLocation() {
		return oldLocation;
	}

	public @NotNull Location getNewLocation() {
		return newLocation;
	}
}
