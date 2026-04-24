package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionDisplaynameUpdateEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Region region;
	private final OfflinePlayer player;
	private final String oldDisplayname;
	private final String newDisplayname;

	public RegionDisplaynameUpdateEvent(@NotNull Region region, @NotNull OfflinePlayer player, @NotNull String oldDisplayname, @NotNull String newDisplayname) {
		this.region = region;
		this.player = player;
		this.oldDisplayname = oldDisplayname;
		this.newDisplayname = newDisplayname;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @NotNull OfflinePlayer getPlayer() {
		return player;
	}

	public @NotNull String getOldDisplayname() {
		return oldDisplayname;
	}

	public @NotNull String getNewDisplayname() {
		return newDisplayname;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}
