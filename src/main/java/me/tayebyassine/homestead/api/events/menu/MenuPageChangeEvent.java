package me.tayebyassine.homestead.api.events.menu;

import me.tayebyassine.homestead.api.events.APIEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player navigates to a different page in a paginated menu.
 * Cancel this event to prevent the page transition.
 */
public class MenuPageChangeEvent extends APIEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String menuKey;
    private final int fromPage;
    private final int toPage;
    private boolean cancelled = false;

    public MenuPageChangeEvent(@NotNull Player player, @NotNull String menuKey, int fromPage, int toPage) {
        this.player = player;
        this.menuKey = menuKey;
        this.fromPage = fromPage;
        this.toPage = toPage;
    }

    public @NotNull Player getPlayer() { return player; }
    public @NotNull String getMenuKey() { return menuKey; }
    public int getFromPage() { return fromPage; }
    public int getToPage() { return toPage; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
