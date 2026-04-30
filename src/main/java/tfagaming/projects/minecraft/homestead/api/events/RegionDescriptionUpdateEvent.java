package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionDescriptionUpdateEvent extends APIEvent {
	private final Region region;
	private final String oldDescription;
	private final String newDescription;

	public RegionDescriptionUpdateEvent(@NotNull Region region, @NotNull String oldDescription, @NotNull String newDescription) {
		this.region = region;
		this.oldDescription = oldDescription;
		this.newDescription = newDescription;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull String getOldDescription() {
		return oldDescription;
	}

	public @NotNull String getNewDescription() {
		return newDescription;
	}
}
