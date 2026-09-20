package me.tayebyassine.homestead.gui;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.MenuButtonRenderEvent;
import me.tayebyassine.homestead.api.events.MenuClickEvent;
import me.tayebyassine.homestead.api.events.MenuCloseEvent;
import me.tayebyassine.homestead.api.events.MenuOpenEvent;
import me.tayebyassine.homestead.api.events.MenuPageChangeEvent;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.gui.helpers.MenuTitles;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Paginated menu class with page navigation support.
 *
 * <p>Supports next/previous page buttons, item-based content with automatic page slicing, action
 * buttons in the bottom row, and dynamic slot replacement. Handles page overflow and empty slot
 * filling automatically. Registers itself as a Bukkit {@link Listener} on construction and
 * unregisters on close.</p>
 */
public class PaginationMenu implements Listener {

    private final Homestead plugin;
    private final String title;
    private final int size;
    private final int contentSize;
    private final ItemStack nextPageItem;
    private final ItemStack prevPageItem;
    private final List<ItemStack> items;
    private final Map<Integer, BiConsumer<Player, InventoryClickEvent>> bottomRowActions = new HashMap<>();
    private final Map<Integer, ItemStack> bottomRowActionItems = new HashMap<>();
    private final BiConsumer<Player, InventoryClickEvent> goBackCallback;
    private final BiConsumer<Player, ClickContext> clickCallback;
    private final int itemsPerPage;
    private final Consumer<Inventory> openHandler;
    private final ItemStack filler;

    private int currentPage = 0;
    private boolean pageChanged = false;
    private Player viewer;
    private final String menuKey;

    private PaginationMenu(Builder builder) {
        this.plugin = Homestead.getInstance();
        this.title = builder.title;
        this.size = builder.size;
        this.contentSize = size - 18;
        this.nextPageItem = builder.nextPageItem;
        this.prevPageItem = builder.prevPageItem;
        this.items = new ArrayList<>(builder.items);
        this.goBackCallback = builder.goBackCallback;
        this.clickCallback = builder.clickCallback;
        this.itemsPerPage = builder.itemsPerPage;
        this.openHandler = builder.openHandler;
        this.filler = builder.filler;
        this.menuKey = builder.menuKey;


        this.bottomRowActions.putAll(builder.bottomRowActions);
        this.bottomRowActionItems.putAll(builder.bottomRowActionItems);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Creates a new {@link Builder} for the given menu key and inventory size.
     *
     * @param menuKey the menu title key
     * @param size    the inventory size (must be a multiple of 9, at least 36)
     * @return a new Builder instance
     */
    public static Builder builder(String menuKey, int size) {
        return new Builder(menuKey, size);
    }

    /**
     * Creates a new {@link Builder} with a placeholder for the menu title.
     *
     * @param menuKey          the menu title key
     * @param titlePlaceholder the placeholder to apply to the title
     * @param size             the inventory size (must be a multiple of 9, at least 36)
     * @return a new Builder instance
     */
    public static Builder builder(String menuKey, Placeholder titlePlaceholder, int size) {
        return new Builder(menuKey, titlePlaceholder, size);
    }

    /**
     * Adds an action button to the bottom row of the menu.
     *
     * @param index    the button index (0, 1, or 2)
     * @param item     the item to display
     * @param callback the click callback
     * @return this menu instance
     */
    public PaginationMenu addActionButton(int index, ItemStack item, BiConsumer<Player, InventoryClickEvent> callback) {
        int[] validSlots = {size - 6, size - 5, size - 4};

        if (index < 0 || index >= validSlots.length) {
            throw new IllegalArgumentException("Invalid index. Only 0, 1, or 2 are allowed.");
        }

        int slot = validSlots[index];
        bottomRowActions.put(slot, callback);
        bottomRowActionItems.put(slot, item);
        return this;
    }

    /**
     * Replaces an item in the content list and updates the viewer's inventory if visible.
     *
     * @param index   the content index to replace
     * @param newItem the new item
     * @return this menu instance
     */
    public PaginationMenu replaceSlot(int index, ItemStack newItem) {
        if (index < 0 || index >= items.size()) return this;

        items.set(index, newItem);

        int start = currentPage * getButtonsPerPage();
        int end = Math.min(start + getButtonsPerPage(), items.size());

        if (index >= start && index < end && viewer != null) {
            int slot = (index - start) + 9;
            viewer.getOpenInventory().getTopInventory().setItem(slot, newItem);
        }
        return this;
    }

    /**
     * Replaces all items in the menu and refreshes the view if open.
     *
     * @param newItems the new item list
     * @return this menu instance
     */
    public PaginationMenu setItems(List<ItemStack> newItems) {
        boolean wasOpen = viewer != null && viewer.getOpenInventory() != null;

        this.items.clear();
        this.items.addAll(newItems);

        int totalPages = getTotalPages();
        if (currentPage >= totalPages && totalPages > 0) {
            currentPage = totalPages - 1;
        } else if (totalPages == 0) {
            currentPage = 0;
        }

        if (wasOpen) {
            refresh();
        }
        return this;
    }

    /**
     * Opens the menu for the specified player.
     *
     * @param player the player to open the menu for
     */
    public void open(Player player) {
        this.viewer = player;
        Inventory pageInv = createPage(currentPage);
        for (int i = 0; i < pageInv.getSize(); i++) {
            ItemStack item = pageInv.getItem(i);
            if (item != null) {
                MenuButtonRenderEvent renderEvent = new MenuButtonRenderEvent(player, menuKey, i, item);
                Homestead.callEvent(renderEvent);
                if (renderEvent.isCancelled()) {
                    pageInv.setItem(i, null);
                } else if (renderEvent.getItem() != item) {
                    pageInv.setItem(i, renderEvent.getItem());
                }
            }
        }
        player.openInventory(pageInv);
        InventoryManager.register(player, this);
        Homestead.callEvent(new MenuOpenEvent(player, menuKey, pageInv, true));
    }

    /**
     * Unregisters all event listeners for this menu.
     */
    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Returns the total number of pages based on the current items.
     *
     * @return the total page count
     */
    public int getTotalPages() {
        return (int) Math.ceil((double) items.size() / getButtonsPerPage());
    }

    private int getButtonsPerPage() {
        return itemsPerPage > 0 ? itemsPerPage : contentSize;
    }

    private void refresh() {
        InventoryManager.unregister(viewer);
        Inventory newInventory = createPage(currentPage);
        pageChanged = true;
        viewer.openInventory(newInventory);
        InventoryManager.register(viewer, this);
        plugin.runSyncTaskLater(() -> pageChanged = false, 1);
    }

    private Inventory createPage(int page) {
        Inventory inv = Bukkit.createInventory(null, size,
                ColorTranslator.translate(Formatter.formatPaginationMenuTitle(title, page + 1, getTotalPages())));

        int buttonsPerPage = getButtonsPerPage();
        int start = page * buttonsPerPage;
        int end = Math.min(start + buttonsPerPage, items.size());


        for (int i = start, slot = 9; i < end; i++, slot++) {
            inv.setItem(slot, items.get(i));
        }


        if (page > 0) {
            inv.setItem(size - 9, prevPageItem);
        } else {
            inv.setItem(size - 9, MenuButtons.getBackButton());
        }

        if ((page + 1) * buttonsPerPage < items.size()) {
            inv.setItem(size - 1, nextPageItem);
        }


        int[] validSlots = {size - 6, size - 5, size - 4};
        for (int validSlot : validSlots) {
            if (bottomRowActionItems.containsKey(validSlot)) {
                inv.setItem(validSlot, bottomRowActionItems.get(validSlot));
            }
        }


        if (filler != null) {
            for (int i = 0; i < 9; i++) {
                inv.setItem(i, filler);
            }
            for (int i = size - 9; i < size; i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, filler);
                }
            }
        }

        if (openHandler != null) openHandler.accept(inv);

        return inv;
    }

    /**
     * Handles inventory click events, routing to page navigation, action buttons, or content callbacks.
     *
     * @param event the inventory click event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (InventoryManager.getMenu(player) != this) return;

        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getSize() != size) return;

        if (!InventoryManager.recordAndCheckClick(player)) {
            event.setCancelled(true);
            return;
        }

        if (InventoryManager.isOnCooldown(player)) {
            event.setCancelled(true);
            return;
        }
        InventoryManager.updateCooldown(player);

        event.setCancelled(true);
        int slot = event.getRawSlot();

        ItemStack clickedItem = slot >= 0 && slot < size ? event.getView().getTopInventory().getItem(slot) : null;
        MenuClickEvent menuClickEvent = new MenuClickEvent(player, menuKey, slot, event.getClick(), clickedItem,
                bottomRowActions.containsKey(slot) || (slot >= 9 && slot < size - 9));
        Homestead.callEvent(menuClickEvent);
        if (menuClickEvent.isCancelled()) return;

        if (bottomRowActions.containsKey(slot)) {
            bottomRowActions.get(slot).accept(player, event);
            return;
        }


        if (slot < 9) return;


        if (slot == size - 9) {
            if (currentPage > 0) {
                int fromPage = currentPage;
                currentPage--;
                MenuPageChangeEvent pageEvent = new MenuPageChangeEvent(player, menuKey, fromPage, currentPage);
                Homestead.callEvent(pageEvent);
                if (pageEvent.isCancelled()) {
                    currentPage = fromPage;
                    return;
                }
                refresh();
            } else {
                plugin.runPlayerTask(player, () -> {
                    if (goBackCallback != null) goBackCallback.accept(player, event);
                    destroy();
                });
            }
            return;
        }

        int buttonsPerPage = getButtonsPerPage();


        if (slot == size - 1 && (currentPage + 1) * buttonsPerPage < items.size()) {
            int fromPage = currentPage;
            currentPage++;
            MenuPageChangeEvent pageEvent = new MenuPageChangeEvent(player, menuKey, fromPage, currentPage);
            Homestead.callEvent(pageEvent);
            if (pageEvent.isCancelled()) {
                currentPage = fromPage;
                return;
            }
            refresh();
            return;
        }


        if (slot >= 9 && slot < size - 9) {
            int itemIndex = currentPage * buttonsPerPage + (slot - 9);
            if (itemIndex < items.size() && clickCallback != null) {
                clickCallback.accept(player, new ClickContext(event, itemIndex, items, this));
            }
        }
    }

    /**
     * Cleans up the menu when the inventory is closed, unless a page transition is in progress.
     *
     * @param event the inventory close event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (pageChanged) {
            pageChanged = false;
            return;
        }

        Player player = (Player) event.getPlayer();
        if (InventoryManager.getMenu(player) == this) {
            Homestead.callEvent(new MenuCloseEvent(player, menuKey));
            InventoryManager.unregister(player);
            destroy();
        }
    }

    public static final class Builder {
        private final int size;
        private final Map<Integer, BiConsumer<Player, InventoryClickEvent>> bottomRowActions = new HashMap<>();
        private final Map<Integer, ItemStack> bottomRowActionItems = new HashMap<>();
        private final String title;
        private String menuKey;
        private ItemStack nextPageItem;
        private ItemStack prevPageItem;
        private List<ItemStack> items = new ArrayList<>();
        private BiConsumer<Player, InventoryClickEvent> goBackCallback;
        private BiConsumer<Player, ClickContext> clickCallback;
        private int itemsPerPage = -1;
        private Consumer<Inventory> openHandler;
        private ItemStack filler;

        private Builder(String menuKey, int size) {
            this(menuKey, null, size);
        }

        private Builder(String menuKey, Placeholder placeholder, int size) {
            if (size % 9 != 0 || size < 36) {
                throw new IllegalArgumentException("Inventory size must be a multiple of 9 and at least 36.");
            }
            this.title = MenuTitles.getTitle(menuKey, placeholder);
            this.size = size;
            this.menuKey = menuKey;
        }

        /**
         * Sets the item displayed for the next-page button.
         *
         * @param item the next-page item
         * @return this builder
         */
        public Builder nextPageItem(ItemStack item) {
            this.nextPageItem = item;
            return this;
        }

        /**
         * Sets the item displayed for the previous-page button.
         *
         * @param item the previous-page item
         * @return this builder
         */
        public Builder prevPageItem(ItemStack item) {
            this.prevPageItem = item;
            return this;
        }

        /**
         * Sets the items to display across all pages.
         *
         * @param items the content items
         * @return this builder
         */
        public Builder items(List<ItemStack> items) {
            this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
            return this;
        }

        /**
         * Sets the callback invoked when the go-back button (slot 0) is clicked.
         *
         * @param callback the go-back callback
         * @return this builder
         */
        public Builder goBack(BiConsumer<Player, InventoryClickEvent> callback) {
            this.goBackCallback = callback;
            return this;
        }

        /**
         * Sets the callback invoked when a content item is clicked.
         *
         * @param callback the click callback
         * @return this builder
         */
        public Builder onClick(BiConsumer<Player, ClickContext> callback) {
            this.clickCallback = callback;
            return this;
        }

        /**
         * Sets the number of items displayed per page.
         *
         * @param n items per page (defaults to content area size)
         * @return this builder
         */
        public Builder itemsPerPage(int n) {
            this.itemsPerPage = n;
            return this;
        }

        /**
         * Sets a handler invoked after the inventory is created, before it is shown.
         *
         * @param handler the open handler
         * @return this builder
         */
        public Builder onOpen(Consumer<Inventory> handler) {
            this.openHandler = handler;
            return this;
        }

        /**
         * Fills empty header and footer slots with the given filler item.
         *
         * @param filler the filler item
         * @return this builder
         */
        public Builder fillEmptySlots(ItemStack filler) {
            this.filler = filler;
            return this;
        }

        /**
         * Fills empty header and footer slots with the default empty-slot item.
         *
         * @return this builder
         */
        public Builder fillEmptySlots() {
            return fillEmptySlots(MenuButtons.getEmptySlot());
        }

        /**
         * Adds an action button to the bottom row of the menu.
         *
         * @param index    the button index (0, 1, or 2)
         * @param item     the item to display
         * @param callback the click callback (may be null)
         * @return this builder
         */
        public Builder actionButton(int index, ItemStack item, BiConsumer<Player, InventoryClickEvent> callback) {
            int[] validSlots = {size - 6, size - 5, size - 4};
            if (index < 0 || index >= validSlots.length) {
                throw new IllegalArgumentException("Invalid index. Only 0, 1, or 2 are allowed.");
            }
            int slot = validSlots[index];
            bottomRowActionItems.put(slot, item);
            if (callback != null) {
                bottomRowActions.put(slot, callback);
            }
            return this;
        }

        /**
         * Builds the {@link PaginationMenu}. Next-page and previous-page items must be set.
         *
         * @return the built PaginationMenu
         * @throws IllegalStateException if next-page or previous-page items are not set
         */
        public PaginationMenu build() {
            if (nextPageItem == null || prevPageItem == null) {
                throw new IllegalStateException("nextPageItem and prevPageItem must be set");
            }
            return new PaginationMenu(this);
        }
    }

    /**
     * Context passed to content click callbacks.
     *
     * @param event    the raw inventory click event
     * @param index    the index of the clicked item in the content list
     * @param items    the current content list
     * @param instance the menu instance
     */
    public record ClickContext(InventoryClickEvent event, int index, List<ItemStack> items, PaginationMenu instance) {
    }
}