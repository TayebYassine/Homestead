package me.tayebyassine.homestead.api.events.communication;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


/**
 * Fired when a player sends a message in region chat.
 */
public class RegionChatEvent extends me.tayebyassine.homestead.api.events.APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final OfflinePlayer player;
    private final String message;

    public RegionChatEvent(@NotNull Region region, @NotNull OfflinePlayer player, @NotNull String message) {
        this.region = region;
        this.player = player;
        this.message = message;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @NotNull OfflinePlayer getPlayer() {
        return player;
    }

    public @NotNull String getMessage() {
        return message;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
