package me.tayebyassine.homestead.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a button is being rendered in a Homestead menu, before the player sees it.
 * Cancel this event to remove the button from the menu, or modify the ItemStack to change its appearance.
 */
public class MenuButtonRenderEvent extends APIEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String menuKey;
    private final int slot;
    private ItemStack item;
    private boolean cancelled = false;

    public MenuButtonRenderEvent(@NotNull Player player, @NotNull String menuKey, int slot, @NotNull ItemStack item) {
        this.player = player;
        this.menuKey = menuKey;
        this.slot = slot;
        this.item = item;
    }

    public @NotNull Player getPlayer() { return player; }
    public @NotNull String getMenuKey() { return menuKey; }
    public int getSlot() { return slot; }

    public @NotNull ItemStack getItem() { return item; }
    public void setItem(@NotNull ItemStack item) { this.item = item; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
