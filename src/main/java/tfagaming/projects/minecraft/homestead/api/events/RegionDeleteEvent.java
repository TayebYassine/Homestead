package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionDeleteEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Region region;
	private final OfflinePlayer player;

	public RegionDeleteEvent(@NotNull Region region, @Nullable OfflinePlayer player) {
		this.region = region;
		this.player = player;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public @NotNull Region getRegion() {
		return region;
	}

	public @Nullable OfflinePlayer getPlayer() {
		return player;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}
