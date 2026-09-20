package me.tayebyassine.homestead.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Fired after a Homestead menu is opened for a player.
 */
public class MenuOpenEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String menuKey;
    private final Inventory inventory;
    private final boolean paginated;

    public MenuOpenEvent(@NotNull Player player, @NotNull String menuKey, @NotNull Inventory inventory, boolean paginated) {
        this.player = player;
        this.menuKey = menuKey;
        this.inventory = inventory;
        this.paginated = paginated;
    }

    public @NotNull Player getPlayer() { return player; }
    public @NotNull String getMenuKey() { return menuKey; }
    public @NotNull Inventory getInventory() { return inventory; }
    public boolean isPaginated() { return paginated; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
