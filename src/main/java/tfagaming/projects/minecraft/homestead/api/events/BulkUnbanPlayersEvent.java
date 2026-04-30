package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;

import java.util.List;

public class BulkUnbanPlayersEvent extends APIEvent {
	private final Region region;

	public BulkUnbanPlayersEvent(@NotNull Region region) {
		this.region = region;
	}

	public @NotNull Region getRegion() {
		return region;
	}
}
