package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionDescriptionUpdateEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Region region;
	private final OfflinePlayer player;
	private final String oldDescription;
	private final String newDescription;

	public RegionDescriptionUpdateEvent(@NotNull Region region, @NotNull OfflinePlayer player, @NotNull String oldDescription, @NotNull String newDescription) {
		this.region = region;
		this.player = player;
		this.oldDescription = oldDescription;
		this.newDescription = newDescription;
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

	public @NotNull String getOldDescription() {
		return oldDescription;
	}

	public @NotNull String getNewDescription() {
		return newDescription;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}
