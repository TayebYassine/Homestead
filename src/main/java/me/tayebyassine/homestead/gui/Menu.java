package me.tayebyassine.homestead.gui;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.MenuButtonRenderEvent;
import me.tayebyassine.homestead.api.events.MenuClickEvent;
import me.tayebyassine.homestead.api.events.MenuCloseEvent;
import me.tayebyassine.homestead.api.events.MenuOpenEvent;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.gui.helpers.MenuTitles;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Base menu class providing a builder-pattern API for creating non-paginated Bukkit inventory
 * menus.
 *
 * <p>Supports button callbacks, item-only slots, fill-empty-slots, back button, and automatic
 * self-registration as a Bukkit {@link Listener}. Each menu instance registers itself on build
 * and unregisters on close.</p>
 */
public class Menu implements Listener {

    protected final Homestead plugin;
    protected final Inventory inventory;
    protected final Map<Integer, BiConsumer<Player, InventoryClickEvent>> callbacks = new HashMap<>();
    protected boolean passthrough;
    protected final String menuKey;

    protected Menu(Builder<?> builder) {
        this.plugin = Homestead.getInstance();
        this.inventory = Bukkit.createInventory(null, builder.size, ColorTranslator.translate(builder.title));
        this.passthrough = builder.passthrough;
        this.menuKey = builder.menuKey;

        for (Map.Entry<Integer, ItemStack> entry : builder.items.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }

        callbacks.putAll(builder.buttons);

        if (builder.filler != null) {
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, builder.filler);
                }
            }
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Creates a new builder for a menu with the given title key and size.
     *
     * @param menuKey the key used to resolve the menu title from config
     * @param size    the inventory size (must be a multiple of 9 between 9 and 54)
     * @return a new {@link Builder} instance
     */
    public static Builder<?> builder(String menuKey, int size) {
        return new Builder<>(menuKey, size);
    }

    /**
     * Creates a new builder with a placeholder applied to the menu title.
     *
     * @param menuKey     the key used to resolve the menu title from config
     * @param placeholder the placeholder to apply to the title
     * @param size        the inventory size (must be a multiple of 9 between 9 and 54)
     * @return a new {@link Builder} instance
     */
    public static Builder<?> builder(String menuKey, Placeholder placeholder, int size) {
        return new Builder<>(menuKey, placeholder, size);
    }

    /**
     * Creates a new builder with passthrough mode enabled or disabled.
     *
     * @param menuKey     the key used to resolve the menu title from config
     * @param size        the inventory size (must be a multiple of 9 between 9 and 54)
     * @param passthrough whether clicks should pass through to the player inventory
     * @return a new {@link Builder} instance
     */
    public static Builder<?> builder(String menuKey, int size, boolean passthrough) {
        return new Builder<>(menuKey, null, size, passthrough);
    }

    /**
     * Creates a new builder with a placeholder and passthrough mode.
     *
     * @param menuKey     the key used to resolve the menu title from config
     * @param placeholder the placeholder to apply to the title
     * @param size        the inventory size (must be a multiple of 9 between 9 and 54)
     * @param passthrough whether clicks should pass through to the player inventory
     * @return a new {@link Builder} instance
     */
    public static Builder<?> builder(String menuKey, Placeholder placeholder, int size, boolean passthrough) {
        return new Builder<>(menuKey, placeholder, size, passthrough);
    }

    /**
     * Sets whether clicks in this menu pass through to the player's inventory.
     *
     * @param passthrough {@code true} to allow clicks to pass through
     * @return this menu instance for chaining
     */
    public Menu setPassthrough(boolean passthrough) {
        this.passthrough = passthrough;
        return this;
    }

    /**
     * Places an item in the specified slot of this menu.
     *
     * @param slot the slot index to place the item in
     * @param item the item to place
     * @return this menu instance for chaining
     */
    public Menu setItem(int slot, ItemStack item) {
        if (isValidSlot(slot)) {
            inventory.setItem(slot, item);
        }
        return this;
    }

    /**
     * Places a clickable button in the specified slot with an associated callback.
     *
     * @param slot     the slot index for the button
     * @param item     the item to display
     * @param callback the action to execute when the button is clicked, or {@code null} for no action
     * @return this menu instance for chaining
     */
    public Menu setButton(int slot, ItemStack item, BiConsumer<Player, InventoryClickEvent> callback) {
        if (!isValidSlot(slot)) return this;

        inventory.setItem(slot, item);
        if (callback != null) {
            callbacks.put(slot, callback);
        }
        return this;
    }

    /**
     * Fills all empty slots in the inventory with the given filler item.
     *
     * @param filler the item to fill empty slots with
     * @return this menu instance for chaining
     */
    public Menu fillEmptySlots(ItemStack filler) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
        return this;
    }

    /**
     * Fills all empty slots with the default empty slot item from {@link MenuButtons}.
     *
     * @return this menu instance for chaining
     */
    public Menu fillEmptySlots() {
        return fillEmptySlots(MenuButtons.getEmptySlot());
    }

    /**
     * Opens this menu for the specified player and registers the player with {@link InventoryManager}.
     *
     * @param player the player to open the menu for
     */
    public void open(Player player) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                MenuButtonRenderEvent renderEvent = new MenuButtonRenderEvent(player, menuKey, i, item);
                Homestead.callEvent(renderEvent);
                if (renderEvent.isCancelled()) {
                    inventory.setItem(i, null);
                } else if (renderEvent.getItem() != item) {
                    inventory.setItem(i, renderEvent.getItem());
                }
            }
        }
        player.openInventory(inventory);
        InventoryManager.register(player, this);
        Homestead.callEvent(new MenuOpenEvent(player, menuKey, inventory, false));
    }

    /**
     * Unregisters this menu as a Bukkit event listener.
     */
    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Handles inventory click events, delegating registered button callbacks.
     * Cancels the event unless passthrough is enabled.
     *
     * @param event the inventory click event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (InventoryManager.getMenu(player) != this) return;
        if (!event.getInventory().equals(this.inventory)) return;

        if (!InventoryManager.recordAndCheckClick(player)) {
            event.setCancelled(true);
            return;
        }

        if (InventoryManager.isOnCooldown(player)) {
            event.setCancelled(true);
            return;
        }
        InventoryManager.updateCooldown(player);

        if (passthrough) return;

        event.setCancelled(true);
        if (event.getClick() == ClickType.MIDDLE) return;

        int slot = event.getRawSlot();
        ItemStack clickedItem = slot >= 0 && slot < inventory.getSize() ? inventory.getItem(slot) : null;
        MenuClickEvent menuClickEvent = new MenuClickEvent(player, menuKey, slot, event.getClick(), clickedItem, callbacks.containsKey(slot));
        Homestead.callEvent(menuClickEvent);
        if (menuClickEvent.isCancelled()) return;

        BiConsumer<Player, InventoryClickEvent> action = callbacks.get(slot);
        if (action != null) {
            plugin.runPlayerTask(player, () -> action.accept(player, event));
        }
    }

    /**
     * Handles inventory close events, unregistering the player and destroying this menu.
     *
     * @param event the inventory close event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (InventoryManager.getMenu(player) == this) {
            Homestead.callEvent(new MenuCloseEvent(player, menuKey));
            InventoryManager.unregister(player);
            destroy();
        }
    }

    /**
     * Checks whether the given slot index is within the valid range for this inventory.
     *
     * @param slot the slot index to validate
     * @return {@code true} if the slot is within bounds
     */
    protected boolean isValidSlot(int slot) {
        return slot >= 0 && slot < inventory.getSize();
    }

    public static class Builder<T extends Builder<T>> {
        protected final int size;
        protected final Map<Integer, ItemStack> items = new HashMap<>();
        protected final Map<Integer, BiConsumer<Player, InventoryClickEvent>> buttons = new HashMap<>();
        protected String title;
        protected boolean passthrough;
        protected ItemStack filler;
        protected String menuKey;

        protected Builder(String menuKey, int size) {
            this(menuKey, size, false);
        }

        protected Builder(String menuKey, Placeholder placeholder, int size) {
            this(menuKey, placeholder, size, false);
        }

        protected Builder(String menuKey, int size, boolean passthrough) {
            this(menuKey, null, size, passthrough);
        }

        protected Builder(String menuKey, Placeholder placeholder, int size, boolean passthrough) {
            if (size % 9 != 0 || size < 9 || size > 54) {
                throw new IllegalArgumentException("Inventory size must be a multiple of 9 between 9 and 54.");
            }
            this.title = MenuTitles.getTitle(menuKey, placeholder);
            this.size = size;
            this.menuKey = menuKey;
            this.passthrough = passthrough;
        }

        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        /**
         * Sets whether clicks in this menu pass through to the player's inventory.
         *
         * @param passthrough {@code true} to allow clicks to pass through
         * @return this builder for chaining
         */
        public T passthrough(boolean passthrough) {
            this.passthrough = passthrough;
            return self();
        }

        /**
         * Places an item in the specified slot.
         *
         * @param slot the slot index to place the item in
         * @param item the item to place
         * @return this builder for chaining
         */
        public T item(int slot, ItemStack item) {
            if (slot >= 0 && slot < size) {
                items.put(slot, item);
            }
            return self();
        }

        /**
         * Places a clickable button in the specified slot with an associated callback.
         *
         * @param slot     the slot index for the button
         * @param item     the item to display
         * @param callback the action to execute when the button is clicked, or {@code null} for no action
         * @return this builder for chaining
         */
        public T button(int slot, ItemStack item, BiConsumer<Player, InventoryClickEvent> callback) {
            if (slot >= 0 && slot < size) {
                items.put(slot, item);
                if (callback != null) {
                    buttons.put(slot, callback);
                }
            }
            return self();
        }

        /**
         * Configures the menu to fill all empty slots with the given filler item on build.
         *
         * @param filler the item to fill empty slots with
         * @return this builder for chaining
         */
        public T fillEmptySlots(ItemStack filler) {
            this.filler = filler;
            return self();
        }

        /**
         * Configures the menu to fill all empty slots with the default empty slot item on build.
         *
         * @return this builder for chaining
         */
        public T fillEmptySlots() {
            return fillEmptySlots(MenuButtons.getEmptySlot());
        }

        /**
         * Builds and returns a new {@link Menu} instance from this builder's configuration.
         *
         * @return a fully constructed {@link Menu}
         */
        public Menu build() {
            return new Menu(this);
        }
    }
}
