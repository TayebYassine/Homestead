package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionDisplaynameUpdateEvent extends APIEvent {
	private final Region region;
	private final String oldDisplayname;
	private final String newDisplayname;

	public RegionDisplaynameUpdateEvent(@NotNull Region region, @NotNull String oldDisplayname, @NotNull String newDisplayname) {
		this.region = region;
		this.oldDisplayname = oldDisplayname;
		this.newDisplayname = newDisplayname;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull String getOldDisplayname() {
		return oldDisplayname;
	}

	public @NotNull String getNewDisplayname() {
		return newDisplayname;
	}
}
