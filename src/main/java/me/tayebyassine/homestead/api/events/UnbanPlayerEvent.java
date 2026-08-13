package me.tayebyassine.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import me.tayebyassine.homestead.models.Region;


public class UnbanPlayerEvent extends APIEvent {
	private final Region region;
	private final OfflinePlayer target;

	public UnbanPlayerEvent(@NotNull Region region, @NotNull OfflinePlayer target) {
		this.region = region;
		this.target = target;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull OfflinePlayer getUnbannedPlayer() {
		return target;
	}
}
