package tfagaming.projects.minecraft.homestead.storage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.gui.InventoryManager;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class StorageMenu implements Listener {
	private final Homestead plugin;
	private final Player player;
	private final UUID regionId;
	private final String title;
	private final SharedStorage storage;
	private final Inventory inventory;
	private final Map<Integer, BiConsumer<Player, InventoryClickEvent>> callbacks;
	private final Object menuLock;
	private boolean valid;
	private boolean processingClick;
	private ItemStack cursorItem;

	public StorageMenu(Player player, UUID regionId, String title, int size) {
		this.plugin = Homestead.getInstance();
		this.player = player;
		this.regionId = regionId;
		this.title = title;
		this.storage = StorageManager.getStorage(regionId);
		this.inventory = Bukkit.createInventory(null, size, ColorTranslator.translate(title));
		this.callbacks = new HashMap<>();
		this.menuLock = new Object();
		this.valid = true;
		this.processingClick = false;
		this.cursorItem = null;

		refreshDisplay();
		StorageManager.registerMenu(regionId, this);
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public Player getPlayer() {
		return player;
	}

	public UUID getRegionId() {
		return regionId;
	}

	public boolean isValid() {
		return valid && player.isOnline() && player.getOpenInventory().getTopInventory().equals(inventory);
	}

	public void refreshDisplay() {
		synchronized (menuLock) {
			Map<Integer, ItemStack> items = storage.getAllItems();
			for (int i = 0; i < inventory.getSize(); i++) {
				ItemStack item = items.get(i);
				inventory.setItem(i, item != null ? item.clone() : null);
			}
		}
	}

	public void updateSlot(int slot, ItemStack item) {
		synchronized (menuLock) {
			if (!valid || processingClick) return;
			inventory.setItem(slot, item != null ? item.clone() : null);
		}
	}

	public void open() {
		player.openInventory(inventory);
		InventoryManager.register(player, new Menu(title, 9) {
			@Override
			public void unregister() {
				cleanup();
			}
		}.setPassthrough(true));
	}

	public void forceClose() {
		if (!valid) return;
		valid = false;
		StorageManager.unregisterMenu(regionId, this);
		HandlerList.unregisterAll(this);

		if (cursorItem != null && !cursorItem.getType().isAir()) {
			Map<Integer, ItemStack> leftover = player.getInventory().addItem(cursorItem);
			for (ItemStack item : leftover.values()) {
				player.getWorld().dropItemNaturally(player.getLocation(), item);
			}
			cursorItem = null;
		}

		if (player.getOpenInventory().getTopInventory().equals(inventory)) {
			plugin.runSyncTask(player::closeInventory);
		}
	}

	public void addItem(int slot, ItemStack itemStack, BiConsumer<Player, InventoryClickEvent> callback) {
		if (slot < 0 || slot >= inventory.getSize()) return;
		synchronized (menuLock) {
			inventory.setItem(slot, itemStack);
			if (callback != null) callbacks.put(slot, callback);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		if (!event.getWhoClicked().equals(player)) return;
		if (!event.getInventory().equals(inventory)) return;

		event.setCancelled(true);
		if (event.getClick() == ClickType.MIDDLE || !valid) return;

		int slot = event.getRawSlot();

		if (callbacks.containsKey(slot)) {
			plugin.runPlayerTask(player, () -> callbacks.get(slot).accept(player, event));
			return;
		}

		if (slot < 0) return;

		if (slot >= storage.getSize()) {
			if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
				synchronized (menuLock) {
					processingClick = true;
					try {
						handleShiftClickFromInventory(event);
					} finally {
						plugin.runPlayerTask(player, () -> {
							player.updateInventory();
							processingClick = false;
						});
					}
				}
			}
			return;
		}

		synchronized (menuLock) {
			processingClick = true;
			try {
				handleStorageClick(event, slot);
			} finally {
				plugin.runPlayerTask(player, () -> {
					player.setItemOnCursor(cursorItem);
					player.updateInventory();
					processingClick = false;
				});
			}
		}
	}

	private void handleStorageClick(InventoryClickEvent event, int slot) {
		ClickType clickType = event.getClick();
		ItemStack cursor = event.getCursor();

		switch (clickType) {
			case LEFT -> {
				if (cursor == null || cursor.getType().isAir()) {
					ItemStack item = storage.tryTakeItem(slot);
					if (item != null) {
						cursorItem = item;
						updateSlot(slot, null);
						StorageManager.updateSlot(regionId, slot);
					}
				} else {
					if (storage.tryPlaceItem(slot, cursor)) {
						cursorItem = null;
						updateSlot(slot, cursor);
						StorageManager.updateSlot(regionId, slot);
					} else {
						PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
					}
				}
			}

			case RIGHT -> {
				if (cursor == null || cursor.getType().isAir()) {
					ItemStack stored = storage.getItem(slot);
					if (stored != null && stored.getAmount() > 1) {
						int half = stored.getAmount() / 2;
						ItemStack pickup = stored.clone();
						pickup.setAmount(half);
						ItemStack remaining = stored.clone();
						remaining.setAmount(stored.getAmount() - half);

						if (storage.tryPlaceItem(slot, remaining)) {
							cursorItem = pickup;
							updateSlot(slot, remaining);
							StorageManager.updateSlot(regionId, slot);
						}
					} else if (stored != null) {
						ItemStack item = storage.tryTakeItem(slot);
						if (item != null) {
							cursorItem = item;
							updateSlot(slot, null);
							StorageManager.updateSlot(regionId, slot);
						}
					}
				} else {
					ItemStack stored = storage.getItem(slot);
					if (stored == null) {
						ItemStack single = cursor.clone();
						single.setAmount(1);
						if (storage.tryPlaceItem(slot, single)) {
							cursor.setAmount(cursor.getAmount() - 1);
							if (cursor.getAmount() <= 0) {
								cursorItem = null;
							}
							updateSlot(slot, single);
							StorageManager.updateSlot(regionId, slot);
						}
					} else if (stored.isSimilar(cursor)) {
						int canAdd = Math.min(stored.getMaxStackSize() - stored.getAmount(), 1);
						if (canAdd > 0) {
							ItemStack newStack = stored.clone();
							newStack.setAmount(stored.getAmount() + canAdd);
							if (storage.tryPlaceItem(slot, newStack)) {
								cursor.setAmount(cursor.getAmount() - canAdd);
								if (cursor.getAmount() <= 0) {
									cursorItem = null;
								}
								updateSlot(slot, newStack);
								StorageManager.updateSlot(regionId, slot);
							}
						}
					}
				}
			}

			case SHIFT_LEFT, SHIFT_RIGHT -> {
				ItemStack currentItem = inventory.getItem(slot);
				if (currentItem != null && !currentItem.getType().isAir()) {
					ItemStack item = storage.tryTakeItem(slot);
					if (item != null) {
						Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
						if (leftover.isEmpty()) {
							updateSlot(slot, null);
							StorageManager.updateSlot(regionId, slot);
						} else {
							ItemStack returned = leftover.values().iterator().next();
							storage.tryPlaceItem(slot, returned);
							updateSlot(slot, returned);
							StorageManager.updateSlot(regionId, slot);
						}
					}
				}
			}

			case DROP, CONTROL_DROP -> {
				ItemStack currentItem = inventory.getItem(slot);
				if (currentItem != null && !currentItem.getType().isAir()) {
					ItemStack item = storage.tryTakeItem(slot);
					if (item != null) {
						player.getWorld().dropItemNaturally(player.getLocation(), item);
						updateSlot(slot, null);
						StorageManager.updateSlot(regionId, slot);
					}
				}
			}

			case NUMBER_KEY -> {
				int hotbarSlot = event.getHotbarButton();
				if (hotbarSlot >= 0 && hotbarSlot < 9 && storage.isSlotAvailable(slot)) {
					ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
					ItemStack stored = storage.getItem(slot);

					if (hotbarItem != null && !hotbarItem.getType().isAir()) {
						if (!storage.tryPlaceItem(slot, hotbarItem)) return;
					} else {
						storage.tryTakeItem(slot);
					}

					player.getInventory().setItem(hotbarSlot, stored);
					updateSlot(slot, hotbarItem);
					StorageManager.updateSlot(regionId, slot);
				}
			}

			case SWAP_OFFHAND -> {
				ItemStack offhand = player.getInventory().getItemInOffHand();
				if (storage.isSlotAvailable(slot)) {
					ItemStack stored = storage.getItem(slot);
					if (offhand != null && !offhand.getType().isAir()) {
						if (!storage.tryPlaceItem(slot, offhand)) return;
					} else {
						storage.tryTakeItem(slot);
					}
					player.getInventory().setItemInOffHand(stored);
					updateSlot(slot, offhand);
					StorageManager.updateSlot(regionId, slot);
				}
			}
		}
		StorageManager.saveStorage(regionId);
	}

	private void handleShiftClickFromInventory(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		if (item == null || item.getType().isAir()) return;

		ItemStack remaining = item.clone();

		for (int i = 0; i < storage.getSize() && remaining.getAmount() > 0; i++) {
			if (callbacks.containsKey(i)) continue;
			ItemStack stored = storage.getItem(i);
			if (stored == null || !stored.isSimilar(remaining)) continue;
			int canAdd = stored.getMaxStackSize() - stored.getAmount();
			if (canAdd <= 0) continue;
			int toAdd = Math.min(canAdd, remaining.getAmount());
			ItemStack newStack = stored.clone();
			newStack.setAmount(stored.getAmount() + toAdd);
			if (storage.tryPlaceItem(i, newStack)) {
				remaining.setAmount(remaining.getAmount() - toAdd);
				updateSlot(i, newStack);
				StorageManager.updateSlot(regionId, i);
			}
		}

		for (int i = 0; i < storage.getSize() && remaining.getAmount() > 0; i++) {
			if (callbacks.containsKey(i)) continue;
			if (storage.getItem(i) != null) continue;
			ItemStack toPlace = remaining.clone();
			if (storage.tryPlaceItem(i, toPlace)) {
				remaining.setAmount(0);
				updateSlot(i, toPlace);
				StorageManager.updateSlot(regionId, i);
			}
		}

		player.getInventory().setItem(event.getSlot(), remaining.getAmount() > 0 ? remaining : null);
		StorageManager.saveStorage(regionId);
		player.updateInventory();
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent event) {
		if (!(event.getWhoClicked() instanceof Player p) || !p.equals(player)) return;
		if (!event.getInventory().equals(inventory)) return;

		boolean affectsStorage = event.getRawSlots().stream()
				.anyMatch(slot -> slot >= 0 && slot < storage.getSize() && !callbacks.containsKey(slot));

		if (!affectsStorage) return; // Doesn't touch storage, let Bukkit handle it normally

		event.setCancelled(true); // Cancel and handle manually to keep SharedStorage in sync

		ItemStack oldCursor = event.getOldCursor();
		if (oldCursor == null || oldCursor.getType().isAir()) return;

		int totalPlaced = 0;

		for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
			int slot = entry.getKey();
			if (slot < 0 || slot >= storage.getSize() || callbacks.containsKey(slot)) continue;

			ItemStack afterDrag = entry.getValue();
			ItemStack current = storage.getItem(slot);
			int currentAmount = (current != null) ? current.getAmount() : 0;
			int amountAdded = afterDrag.getAmount() - currentAmount;

			if (amountAdded > 0 && storage.tryPlaceItem(slot, afterDrag)) {
				totalPlaced += amountAdded;
				updateSlot(slot, afterDrag);
				StorageManager.updateSlot(regionId, slot);
			}
		}

		if (totalPlaced > 0) {
			int remaining = oldCursor.getAmount() - totalPlaced;
			if (remaining <= 0) {
				event.setCursor(null);
				cursorItem = null;
			} else {
				ItemStack newCursor = oldCursor.clone();
				newCursor.setAmount(remaining);
				event.setCursor(newCursor);
				cursorItem = newCursor;
			}
			StorageManager.saveStorage(regionId);
			plugin.runPlayerTask(player, () -> {
				player.setItemOnCursor(cursorItem);
				player.updateInventory();
			});
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) return;
		if (!event.getPlayer().equals(player)) return;
		if (!event.getInventory().equals(inventory)) return;
		cleanup();
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (!event.getPlayer().equals(player)) return;
		cleanup();
	}

	private void cleanup() {
		if (!valid) return;
		valid = false;
		StorageManager.saveStorage(regionId);
		StorageManager.unregisterMenu(regionId, this);
		HandlerList.unregisterAll(this);
		InventoryManager.unregister(player);

		if (cursorItem != null && !cursorItem.getType().isAir() && player.isOnline()) {
			Map<Integer, ItemStack> leftover = player.getInventory().addItem(cursorItem);
			for (ItemStack item : leftover.values()) {
				player.getWorld().dropItemNaturally(player.getLocation(), item);
			}
		}
		cursorItem = null;
	}
}