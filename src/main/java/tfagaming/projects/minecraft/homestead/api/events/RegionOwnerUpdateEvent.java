package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionOwnerUpdateEvent extends APIEvent {
	private final Region region;
	private final OfflinePlayer oldOwner;
	private final OfflinePlayer newOwner;

	public RegionOwnerUpdateEvent(@NotNull Region region, @NotNull OfflinePlayer oldOwner, @NotNull OfflinePlayer newOwner) {
		this.region = region;
		this.oldOwner = oldOwner;
		this.newOwner = newOwner;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull OfflinePlayer getOldOwner() {
		return oldOwner;
	}

	public @NotNull OfflinePlayer getNewOwner() {
		return newOwner;
	}
}
