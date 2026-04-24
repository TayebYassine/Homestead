package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionChatEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Region region;
	private final OfflinePlayer player;
	private final String message;

	public RegionChatEvent(@NotNull Region region, @NotNull OfflinePlayer player, @NotNull String message) {
		this.region = region;
		this.player = player;
		this.message = message;
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

	public String getMessage() {
		return message;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}
