package me.tayebyassine.homestead.api.events.player;

import me.tayebyassine.homestead.models.SubArea;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


/**
 * Fired when a player joins a sub-area within a region.
 */
public class PlayerJoinSubAreaEvent extends me.tayebyassine.homestead.api.events.APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final SubArea subArea;
    private final OfflinePlayer player;

    public PlayerJoinSubAreaEvent(@NotNull SubArea subArea, @NotNull OfflinePlayer player) {
        this.subArea = subArea;
        this.player = player;
    }

    public @NotNull SubArea getSubArea() {
        return subArea;
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
