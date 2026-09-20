package me.tayebyassine.homestead.api.events.region;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


/**
 * Fired when a new region is created. Cancel to prevent creation.
 */
public class RegionCreateEvent extends APIEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
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

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
