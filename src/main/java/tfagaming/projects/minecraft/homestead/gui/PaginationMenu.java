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

import java.util.ArrayList;
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
	private final int itemsPerPage;
	private final Consumer<Inventory> openHandler;
	private final ItemStack filler;

	private int currentPage = 0;
	private boolean pageChanged = false;
	private Player viewer;

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


		this.bottomRowActions.putAll(builder.bottomRowActions);
		this.bottomRowActionItems.putAll(builder.bottomRowActionItems);

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public static Builder builder(int pathTitle, int size) {
		return new Builder(pathTitle, size);
	}

	public static Builder builder(String title, int size) {
		return new Builder(title, size);
	}

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

	public void open(Player player) {
		this.viewer = player;
		player.openInventory(createPage(currentPage));
		InventoryManager.register(player, this);
	}

	public void destroy() {
		HandlerList.unregisterAll(this);
	}

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
			inv.setItem(size - 9, MenuUtility.getBackButton());
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

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) return;
		if (InventoryManager.getMenu(player) != this) return;

		Inventory topInventory = event.getView().getTopInventory();
		if (topInventory.getSize() != size) return;

		if (InventoryManager.isOnCooldown(player)) {
			event.setCancelled(true);
			return;
		}
		InventoryManager.updateCooldown(player);

		event.setCancelled(true);
		int slot = event.getRawSlot();


		if (bottomRowActions.containsKey(slot)) {
			bottomRowActions.get(slot).accept(player, event);
			return;
		}


		if (slot < 9) return;


		if (slot == size - 9 && currentPage > 0) {
			currentPage--;
			refresh();
			return;
		}

		int buttonsPerPage = getButtonsPerPage();


		if (slot == size - 1 && (currentPage + 1) * buttonsPerPage < items.size()) {
			currentPage++;
			refresh();
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
			if (itemIndex < items.size() && clickCallback != null) {
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

	public static final class Builder {
		private final String title;
		private final int size;
		private final Map<Integer, BiConsumer<Player, InventoryClickEvent>> bottomRowActions = new HashMap<>();
		private final Map<Integer, ItemStack> bottomRowActionItems = new HashMap<>();
		private ItemStack nextPageItem;
		private ItemStack prevPageItem;
		private List<ItemStack> items = new ArrayList<>();
		private BiConsumer<Player, InventoryClickEvent> goBackCallback;
		private BiConsumer<Player, ClickContext> clickCallback;
		private int itemsPerPage = -1;
		private Consumer<Inventory> openHandler;
		private ItemStack filler;

		private Builder(int pathTitle, int size) {
			this(MenuUtility.getTitle(pathTitle), size);
		}

		private Builder(String title, int size) {
			if (size % 9 != 0 || size < 36) {
				throw new IllegalArgumentException("Inventory size must be a multiple of 9 and at least 36.");
			}
			this.title = title;
			this.size = size;
		}

		public Builder nextPageItem(ItemStack item) {
			this.nextPageItem = item;
			return this;
		}

		public Builder prevPageItem(ItemStack item) {
			this.prevPageItem = item;
			return this;
		}

		public Builder items(List<ItemStack> items) {
			this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
			return this;
		}

		public Builder goBack(BiConsumer<Player, InventoryClickEvent> callback) {
			this.goBackCallback = callback;
			return this;
		}

		public Builder onClick(BiConsumer<Player, ClickContext> callback) {
			this.clickCallback = callback;
			return this;
		}

		public Builder itemsPerPage(int n) {
			this.itemsPerPage = n;
			return this;
		}

		public Builder onOpen(Consumer<Inventory> handler) {
			this.openHandler = handler;
			return this;
		}

		public Builder fillEmptySlots(ItemStack filler) {
			this.filler = filler;
			return this;
		}

		public Builder fillEmptySlots() {
			return fillEmptySlots(MenuUtility.getEmptySlot());
		}

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

		public PaginationMenu build() {
			if (nextPageItem == null || prevPageItem == null) {
				throw new IllegalStateException("nextPageItem and prevPageItem must be set");
			}
			return new PaginationMenu(this);
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