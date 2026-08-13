package me.tayebyassine.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import me.tayebyassine.homestead.models.Region;


public class RegionCreateEvent extends APIEvent {
	private final Region region;
	private final OfflinePlayer player;

	public RegionCreateEvent(@NotNull Region region, @NotNull OfflinePlayer player) {
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
