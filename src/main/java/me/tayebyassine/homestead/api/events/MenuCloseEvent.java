package me.tayebyassine.homestead.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a Homestead menu is closed by a player.
 */
public class MenuCloseEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String menuKey;

    public MenuCloseEvent(@NotNull Player player, @NotNull String menuKey) {
        this.player = player;
        this.menuKey = menuKey;
    }

    public @NotNull Player getPlayer() { return player; }
    public @NotNull String getMenuKey() { return menuKey; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
