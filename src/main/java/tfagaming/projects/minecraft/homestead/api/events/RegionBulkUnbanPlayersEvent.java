package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.structure.Region;

import java.util.List;

public class RegionBulkUnbanPlayersEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Region region;
	private final OfflinePlayer player;
	private final List<OfflinePlayer> targets;

	public RegionBulkUnbanPlayersEvent(@NotNull Region region, @NotNull OfflinePlayer player, @NotNull List<OfflinePlayer> targets) {
		this.region = region;
		this.player = player;
		this.targets = targets;
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

	public @NotNull List<OfflinePlayer> getUnbannedPlayers() {
		return targets;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}
