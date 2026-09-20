package me.tayebyassine.homestead.api.events.player;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


/**
 * Fired when a player leaves a region they were a member of.
 */
public class PlayerLeftRegionEvent extends me.tayebyassine.homestead.api.events.APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final OfflinePlayer player;

    public PlayerLeftRegionEvent(@NotNull Region region, @NotNull OfflinePlayer player) {
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
}
