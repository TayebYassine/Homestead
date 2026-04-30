package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionNameUpdateEvent extends APIEvent {
	private final Region region;
	private final String oldName;
	private final String newName;

	public RegionNameUpdateEvent(@NotNull Region region, @NotNull String oldName, @NotNull String newName) {
		this.region = region;
		this.oldName = oldName;
		this.newName = newName;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull String getOldName() {
		return oldName;
	}

	public @NotNull String getNewName() {
		return newName;
	}
}
