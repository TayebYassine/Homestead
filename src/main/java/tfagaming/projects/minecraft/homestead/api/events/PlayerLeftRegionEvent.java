package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class PlayerLeftRegionEvent extends APIEvent {
	private final Region region;
	private final OfflinePlayer player;

	public PlayerLeftRegionEvent(@NotNull Region region, @NotNull OfflinePlayer player) {
		this.region = region;
		this.player = player;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull OfflinePlayer getPlayer() {
		return player;
	}
}
