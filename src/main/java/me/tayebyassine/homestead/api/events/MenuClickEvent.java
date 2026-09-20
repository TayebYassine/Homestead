package me.tayebyassine.homestead.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a player clicks inside a Homestead menu.
 * Cancel this event to prevent the default button action from executing.
 */
public class MenuClickEvent extends APIEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String menuKey;
    private final int slot;
    private final ClickType clickType;
    private final ItemStack clickedItem;
    private final boolean hasCallback;
    private boolean cancelled = false;

    public MenuClickEvent(@NotNull Player player, @NotNull String menuKey, int slot,
                          @NotNull ClickType clickType, @Nullable ItemStack clickedItem, boolean hasCallback) {
        this.player = player;
        this.menuKey = menuKey;
        this.slot = slot;
        this.clickType = clickType;
        this.clickedItem = clickedItem;
        this.hasCallback = hasCallback;
    }

    public @NotNull Player getPlayer() { return player; }
    public @NotNull String getMenuKey() { return menuKey; }
    public int getSlot() { return slot; }
    public @NotNull ClickType getClickType() { return clickType; }
    public @Nullable ItemStack getClickedItem() { return clickedItem; }
    public boolean hasCallback() { return hasCallback; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
