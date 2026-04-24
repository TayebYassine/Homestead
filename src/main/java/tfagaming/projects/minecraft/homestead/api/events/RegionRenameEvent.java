package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionRenameEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Region region;
	private final OfflinePlayer player;
	private final String oldName;
	private final String newName;

	public RegionRenameEvent(@NotNull Region region, @NotNull OfflinePlayer player, @NotNull String oldName, @NotNull String newName) {
		this.region = region;
		this.player = player;
		this.oldName = oldName;
		this.newName = newName;
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

	public @NotNull String getOldName() {
		return oldName;
	}

	public @NotNull String getNewName() {
		return newName;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}
