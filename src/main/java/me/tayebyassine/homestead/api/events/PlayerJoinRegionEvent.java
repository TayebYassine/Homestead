package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public class PlayerJoinRegionEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final OfflinePlayer player;

    public PlayerJoinRegionEvent(@NotNull Region region, @NotNull OfflinePlayer player) {
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
