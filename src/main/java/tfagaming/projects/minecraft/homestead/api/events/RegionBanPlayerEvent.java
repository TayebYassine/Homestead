package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.models.Region;


public class RegionBanPlayerEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Region region;
	private final OfflinePlayer player;
	private final OfflinePlayer target;
	private final String reason;

	public RegionBanPlayerEvent(@NotNull Region region, @NotNull OfflinePlayer player, @NotNull OfflinePlayer target, String reason) {
		this.region = region;
		this.player = player;
		this.target = target;
		this.reason = reason;
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

	public @NotNull OfflinePlayer getBannedPlayer() {
		return target;
	}

	public @Nullable String getReason() {
		return reason;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}
