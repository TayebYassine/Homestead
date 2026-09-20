package me.tayebyassine.homestead.api.events.player;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when all banned players are unbanned from a region at once.
 */
public class BulkUnbanPlayersEvent extends me.tayebyassine.homestead.api.events.APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;

    public BulkUnbanPlayersEvent(@NotNull Region region) {
        this.region = region;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
