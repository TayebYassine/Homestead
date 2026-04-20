package tfagaming.projects.minecraft.homestead.gui;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
	private boolean pageChanged = false;
	private Player player;
	private ItemStack fillerItemstack = null;
	private Consumer<Inventory> openHandler;
	private int itemsPerPage = -1;

	private int currentPage;

	/**
	 * Pagination menu constructor.
	 * @param title The menu title
	 * @param size The size of the menu
	 * @param nextPageItem Next page item stack
	 * @param prevPageItem Previous page item stack
	 * @param items The items that will be displayed in all pages, automatic splitting
	 * @param goBackCallback Back button callback
	 * @param clickCallback Action to perform once an item was clicked by a player
	 */
	public PaginationMenu(String title, int size, ItemStack nextPageItem, ItemStack prevPageItem, List<ItemStack> items,
						  BiConsumer<Player, InventoryClickEvent> goBackCallback, BiConsumer<Player, ClickContext> clickCallback) {
		if (size % 9 != 0 || size < 36) {
			throw new IllegalArgumentException("Inventory size must be a multiple of 9 and at least 36.");
		}

		this.plugin = Homestead.getInstance();
		this.title = title;
		this.size = size;
		this.contentSize = size - 18;
		this.nextPageItem = nextPageItem;
		this.prevPageItem = prevPageItem;
		this.items = items;
		this.goBackCallback = goBackCallback;
		this.clickCallback = clickCallback;
		this.currentPage = 0;

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * Add an action button, outside the items list. These buttons will be displayed in the last row of the menu.
	 * @param index The slot index
	 * @param item The item stack
	 * @param callback Action to perform once this action button was clicked by a player
	 */
	public void addActionButton(int index, ItemStack item, BiConsumer<Player, InventoryClickEvent> callback) {
		int[] validSlots = {size - 6, size - 5, size - 4};

		if (index < 0 || index >= validSlots.length) {
			throw new IllegalArgumentException("Invalid index. Only 0, 1, or 2 are allowed.");
		}

		int slot = validSlots[index];

		bottomRowActions.put(slot, callback);
		bottomRowActionItems.put(slot, item);
	}

	public void addOpenHandler(Consumer<Inventory> handler) {
		this.openHandler = handler;
	}

	public void setItemsPerPage(int n) {
		this.itemsPerPage = n;
	}

	/**
	 * Open the menu for a player.
	 * @param player The player
	 */
	public void open(Player player) {
		this.player = player;

		player.openInventory(createPage(currentPage));

		InventoryManager.register(player, this);
	}

	/**
	 * Open the menu for a player and fill empty slots with an item stack.
	 * @param player The player
	 * @param filler The item stack filler
	 */
	public void open(Player player, ItemStack filler) {
		this.player = player;

		Inventory inventory = createPage(currentPage);

		for (int i = 0; i < 9; i++) {
			inventory.setItem(i, filler);
		}

		for (int i = size - 9; i < size; i++) {
			if (inventory.getItem(i) == null) {
				inventory.setItem(i, filler);
			}
		}

		fillerItemstack = filler;

		player.openInventory(inventory);

		InventoryManager.register(player, this);
	}

	/**
	 * Replace an item stack slot with a new item stack.
	 * @param index The slot index
	 * @param newItem The new item stack
	 */
	public void replaceSlot(int index, ItemStack newItem) {
		if (index < 0 || index >= items.size()) {
			return;
		}

		items.set(index, newItem);

		int start = currentPage * contentSize;
		int end = Math.min(start + contentSize, items.size());

		if (index >= start && index < end) {
			int slot = (index - start) + 9;

			if (player != null) {
				Inventory inventory = player.getOpenInventory().getTopInventory();

				inventory.setItem(slot, newItem);
			}
		}
	}

	/**
	 * Update all the items that will be displayed in pages.
	 * @param newItems The new list of item stacks.
	 */
	public void setItems(List<ItemStack> newItems) {
		boolean wasOpen = player != null && player.getOpenInventory() != null;

		this.items.clear();
		this.items.addAll(newItems);

		int totalPages = getTotalPages();

		if (currentPage >= totalPages && totalPages > 0) {
			currentPage = totalPages - 1;
		} else if (totalPages == 0) {
			currentPage = 0;
		}

		if (wasOpen) {
			InventoryManager.unregister(player);

			Inventory newInventory = createPage(currentPage);

			pageChanged = true;

			player.openInventory(newInventory);

			InventoryManager.register(player, this);

			plugin.runSyncTaskLater(() -> {
				pageChanged = false;
			}, 1);
		}
	}

	/**
	 * Unregister the menu instance from the listener.
	 */
	public void destroy() {
		HandlerList.unregisterAll(this);
	}

	public int getTotalPages() {
		int buttonsPerPage = (itemsPerPage > 0) ? itemsPerPage : contentSize;

		return (int) Math.ceil((double) items.size() / buttonsPerPage);
	}

	private Inventory createPage(int page) {
		Inventory inventory = Bukkit.createInventory(null, size,
				ColorTranslator.translate(Formatter.formatPaginationMenuTitle(title, page + 1, getTotalPages())));

		int buttonsPerPage = (itemsPerPage > 0) ? itemsPerPage : contentSize;
		boolean hasNext = (page + 1) * buttonsPerPage < items.size();
		if (hasNext) inventory.setItem(size - 1, nextPageItem);

		int start = page * buttonsPerPage;
		int end = Math.min(start + buttonsPerPage, items.size());

		for (int i = start, slot = 9; i < end; i++, slot++) {
			inventory.setItem(slot, items.get(i));
		}

		if (page > 0) {
			inventory.setItem(size - 9, prevPageItem);
		} else {
			inventory.setItem(size - 9, MenuUtility.getBackButton());
		}

		if (end < items.size()) {
			inventory.setItem(size - 1, nextPageItem);
		}

		int[] validSlots = {size - 6, size - 5, size - 4};

		for (int validSlot : validSlots) {
			if (bottomRowActionItems.containsKey(validSlot)) {
				inventory.setItem(validSlot, bottomRowActionItems.get(validSlot));
			}
		}

		if (fillerItemstack != null) {
			for (int i = 0; i < 9; i++) {
				inventory.setItem(i, fillerItemstack);
			}

			for (int i = size - 9; i < size; i++) {
				if (inventory.getItem(i) == null) {
					inventory.setItem(i, fillerItemstack);
				}
			}
		}

		if (openHandler != null) openHandler.accept(inventory);

		return inventory;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) {
			return;
		}

		if (InventoryManager.getMenu(player) != this) {
			return;
		}

		Inventory topInventory = event.getView().getTopInventory();
		if (topInventory.getSize() != size) {
			return;
		}

		// Cancel silently if the player clicked too recently
		if (InventoryManager.isOnCooldown(player)) {
			event.setCancelled(true);
			return;
		}
		InventoryManager.updateCooldown(player);

		event.setCancelled(true);
		int slot = event.getRawSlot();

		if (bottomRowActions.containsKey(slot) && bottomRowActions.get(slot) != null) {
			bottomRowActions.get(slot).accept(player, event);
			return;
		}

		if (slot < 9) {
			return;
		}

		if (slot == size - 9 && currentPage > 0) {
			currentPage--;
			pageChanged = true;
			player.openInventory(createPage(currentPage));
			return;
		}

		int buttonsPerPage = (itemsPerPage > 0) ? itemsPerPage : contentSize;

		if (slot == size - 1 && (currentPage + 1) * buttonsPerPage < items.size()) {
			currentPage++;
			pageChanged = true;
			player.openInventory(createPage(currentPage));
			return;
		}

		if (slot == size - 9) {
			plugin.runPlayerTask(player, () -> {
				if (goBackCallback != null) goBackCallback.accept(player, event);

				destroy();
			});

			return;
		}

		if (slot >= 9 && slot < size - 9) {
			int itemIndex = currentPage * buttonsPerPage + (slot - 9);

			if (itemIndex < items.size()) {
				if (clickCallback != null)
					clickCallback.accept(player, new ClickContext(event, itemIndex, items, this));
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		if (pageChanged) {
			pageChanged = false;
			return;
		}

		Player player = (Player) event.getPlayer();

		if (InventoryManager.getMenu(player) == this) {
			InventoryManager.unregister(player);
			destroy();
		}
	}

	public static class ClickContext {
		private final InventoryClickEvent event;
		private final int index;
		private final List<ItemStack> items;
		private final PaginationMenu instance;

		public ClickContext(InventoryClickEvent event, int index, List<ItemStack> items, PaginationMenu instance) {
			this.event = event;
			this.index = index;
			this.items = items;
			this.instance = instance;
		}

		public InventoryClickEvent getEvent() {
			return event;
		}

		public int getIndex() {
			return index;
		}

		public List<ItemStack> getItems() {
			return items;
		}

		public PaginationMenu getInstance() {
			return instance;
		}
	}
}