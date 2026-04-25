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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class StorageMenu implements Listener {
	private final Homestead plugin;
	private final Player player;
	private final long regionId;
	private final String title;
	private final SharedStorage storage;
	private final Inventory inventory;
	private final Map<Integer, BiConsumer<Player, InventoryClickEvent>> callbacks;

	private Menu passthroughMenu;

	private boolean valid;
	private ItemStack cursorItem;

	public StorageMenu(Player player, long regionId, String title, int size) {
		this.plugin = Homestead.getInstance();
		this.player = player;
		this.regionId = regionId;
		this.title = title;
		this.storage = StorageManager.getStorage(regionId);
		this.inventory = Bukkit.createInventory(null, size, ColorTranslator.translate(title));
		this.callbacks = new HashMap<>();
		this.valid = true;
		this.cursorItem = null;

		refreshDisplay();
		StorageManager.registerMenu(regionId, this);
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public Player getPlayer() {
		return player;
	}

	public long getRegionId() {
		return regionId;
	}

	public boolean isValid() {
		return valid && player.isOnline() && player.getOpenInventory().getTopInventory().equals(inventory);
	}

	public void refreshDisplay() {
		Map<Integer, ItemStack> items = storage.getAllItems();
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack item = items.get(i);
			inventory.setItem(i, item != null ? item.clone() : null);
		}
	}

	public void updateSlot(int slot, ItemStack item) {
		if (!valid) return;
		inventory.setItem(slot, item != null ? item.clone() : null);
	}

	public void open() {
		player.openInventory(inventory);
		passthroughMenu = new Menu(title, 9).setPassthrough(true);
		InventoryManager.register(player, passthroughMenu);
	}

	public void forceClose() {
		if (!valid) return;
		valid = false;
		StorageManager.unregisterMenu(regionId, this);
		HandlerList.unregisterAll(this);
		unregisterPassthrough();

		if (player.getOpenInventory().getTopInventory().equals(inventory)) {
			plugin.runSyncTask(player::closeInventory);
		}
	}

	public void addItem(int slot, ItemStack itemStack, BiConsumer<Player, InventoryClickEvent> callback) {
		if (slot < 0 || slot >= inventory.getSize()) return;
		inventory.setItem(slot, itemStack);
		if (callback != null) callbacks.put(slot, callback);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		if (!event.getWhoClicked().equals(player)) return;
		if (!event.getInventory().equals(inventory)) return;

		if (event.getClick() == ClickType.MIDDLE || !valid) {
			event.setCancelled(true);
			return;
		}

		int slot = event.getRawSlot();

		if (slot >= 0 && callbacks.containsKey(slot)) {
			event.setCancelled(true);
			plugin.runPlayerTask(player, () -> callbacks.get(slot).accept(player, event));
			return;
		}

		if (slot >= storage.getSize()) {
			if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
				event.setCancelled(true);
				handleShiftClickFromInventory(event);
				plugin.runPlayerTask(player, player::updateInventory);
			}
			return;
		}

		event.setCancelled(true);
		if (slot < 0) return;

		handleStorageClick(event, slot);
		final ItemStack cursorSnapshot = this.cursorItem;
		plugin.runPlayerTask(player, () -> {
			player.setItemOnCursor(cursorSnapshot);
			player.updateInventory();
		});
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent event) {
		if (!(event.getWhoClicked() instanceof Player p) || !p.equals(player)) return;
		if (!event.getInventory().equals(inventory)) return;

		boolean affectsStorage = event.getRawSlots().stream()
				.anyMatch(s -> s >= 0 && s < storage.getSize() && !callbacks.containsKey(s));

		if (!affectsStorage) return;

		event.setCancelled(true);

		ItemStack oldCursor = event.getOldCursor();
		if (oldCursor == null || oldCursor.getType().isAir()) return;

		int totalPlaced = 0;

		for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
			int slot = entry.getKey();
			if (slot < 0 || slot >= storage.getSize() || callbacks.containsKey(slot)) continue;

			ItemStack afterDrag = entry.getValue();
			ItemStack current = storage.getItem(slot);
			int currentAmount = current != null ? current.getAmount() : 0;
			int amountAdded = afterDrag.getAmount() - currentAmount;

			if (amountAdded > 0) {
				storage.placeItem(slot, afterDrag);
				totalPlaced += amountAdded;
				updateSlot(slot, afterDrag);
				StorageManager.updateSlot(regionId, slot);
			}
		}

		if (totalPlaced > 0) {
			int remaining = oldCursor.getAmount() - totalPlaced;
			cursorItem = remaining > 0 ? oldCursor.clone() : null;
			if (cursorItem != null) cursorItem.setAmount(remaining);
			StorageManager.saveStorage(regionId);
			final ItemStack cursorSnapshot = cursorItem;
			plugin.runPlayerTask(player, () -> {
				player.setItemOnCursor(cursorSnapshot);
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

	private void handleStorageClick(InventoryClickEvent event, int slot) {
		ClickType clickType = event.getClick();
		ItemStack cursor = event.getCursor();

		switch (clickType) {

			case LEFT -> {
				if (cursor == null || cursor.getType().isAir()) {
					ItemStack taken = storage.takeItem(slot);
					cursorItem = taken;
					updateSlot(slot, null);
					if (taken != null) StorageManager.updateSlot(regionId, slot);

				} else {
					ItemStack stored = storage.getItem(slot);
					if (stored != null && stored.isSimilar(cursor)) {
						int canAdd = stored.getMaxStackSize() - stored.getAmount();
						int toAdd = Math.min(canAdd, cursor.getAmount());
						if (toAdd > 0) {
							ItemStack merged = stored.clone();
							merged.setAmount(stored.getAmount() + toAdd);
							storage.placeItem(slot, merged);
							int leftover = cursor.getAmount() - toAdd;
							cursorItem = leftover > 0 ? cursor.clone() : null;
							if (cursorItem != null) cursorItem.setAmount(leftover);
							updateSlot(slot, merged);
							StorageManager.updateSlot(regionId, slot);
						}
					} else {
						ItemStack old = storage.swapItem(slot, cursor);
						cursorItem = old;
						updateSlot(slot, cursor);
						StorageManager.updateSlot(regionId, slot);
					}
				}
			}

			case RIGHT -> {
				if (cursor == null || cursor.getType().isAir()) {
					ItemStack stored = storage.getItem(slot);
					if (stored == null) break;

					if (stored.getAmount() > 1) {
						int half = stored.getAmount() / 2;
						ItemStack pickup = stored.clone();
						pickup.setAmount(half);
						ItemStack remaining = stored.clone();
						remaining.setAmount(stored.getAmount() - half);
						storage.placeItem(slot, remaining);
						cursorItem = pickup;
						updateSlot(slot, remaining);
						StorageManager.updateSlot(regionId, slot);
					} else {
						ItemStack taken = storage.takeItem(slot);
						cursorItem = taken;
						updateSlot(slot, null);
						if (taken != null) StorageManager.updateSlot(regionId, slot);
					}

				} else {
					ItemStack stored = storage.getItem(slot);
					if (stored == null || stored.getType().isAir()) {
						ItemStack single = cursor.clone();
						single.setAmount(1);
						storage.placeItem(slot, single);
						int remaining = cursor.getAmount() - 1;
						cursorItem = remaining > 0 ? cursor.clone() : null;
						if (cursorItem != null) cursorItem.setAmount(remaining);
						updateSlot(slot, single);
						StorageManager.updateSlot(regionId, slot);

					} else if (stored.isSimilar(cursor) && stored.getAmount() < stored.getMaxStackSize()) {
						ItemStack grown = stored.clone();
						grown.setAmount(stored.getAmount() + 1);
						storage.placeItem(slot, grown);
						int remaining = cursor.getAmount() - 1;
						cursorItem = remaining > 0 ? cursor.clone() : null;
						if (cursorItem != null) cursorItem.setAmount(remaining);
						updateSlot(slot, grown);
						StorageManager.updateSlot(regionId, slot);
					}
				}
			}

			case SHIFT_LEFT, SHIFT_RIGHT -> {
				ItemStack item = storage.takeItem(slot);
				if (item != null) {
					Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
					if (leftover.isEmpty()) {
						updateSlot(slot, null);
						StorageManager.updateSlot(regionId, slot);
					} else {
						ItemStack returned = leftover.values().iterator().next();
						storage.placeItem(slot, returned);
						updateSlot(slot, returned);
						StorageManager.updateSlot(regionId, slot);
					}
				}
			}

			case DROP -> {
				ItemStack stored = storage.getItem(slot);
				if (stored != null) {
					ItemStack drop = stored.clone();
					drop.setAmount(1);
					player.getWorld().dropItemNaturally(player.getLocation(), drop);
					if (stored.getAmount() > 1) {
						ItemStack remaining = stored.clone();
						remaining.setAmount(stored.getAmount() - 1);
						storage.placeItem(slot, remaining);
						updateSlot(slot, remaining);
					} else {
						storage.takeItem(slot);
						updateSlot(slot, null);
					}
					StorageManager.updateSlot(regionId, slot);
				}
			}

			case CONTROL_DROP -> {
				ItemStack item = storage.takeItem(slot);
				if (item != null) {
					player.getWorld().dropItemNaturally(player.getLocation(), item);
					updateSlot(slot, null);
					StorageManager.updateSlot(regionId, slot);
				}
			}

			case NUMBER_KEY -> {
				int hotbarSlot = event.getHotbarButton();
				if (hotbarSlot >= 0 && hotbarSlot < 9) {
					ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
					ItemStack stored = storage.swapItem(slot, hotbarItem);
					player.getInventory().setItem(hotbarSlot, stored);
					updateSlot(slot, hotbarItem != null && !hotbarItem.getType().isAir() ? hotbarItem : null);
					StorageManager.updateSlot(regionId, slot);
				}
			}

			case SWAP_OFFHAND -> {
				ItemStack offhand = player.getInventory().getItemInOffHand();
				ItemStack stored = storage.swapItem(slot, offhand);
				player.getInventory().setItemInOffHand(stored);
				updateSlot(slot, offhand != null && !offhand.getType().isAir() ? offhand : null);
				StorageManager.updateSlot(regionId, slot);
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
			ItemStack grown = stored.clone();
			grown.setAmount(stored.getAmount() + toAdd);
			storage.placeItem(i, grown);
			remaining.setAmount(remaining.getAmount() - toAdd);
			updateSlot(i, grown);
			StorageManager.updateSlot(regionId, i);
		}

		for (int i = 0; i < storage.getSize() && remaining.getAmount() > 0; i++) {
			if (callbacks.containsKey(i)) continue;
			if (storage.getItem(i) != null) continue;
			ItemStack toPlace = remaining.clone();
			storage.placeItem(i, toPlace);
			remaining.setAmount(0);
			updateSlot(i, toPlace);
			StorageManager.updateSlot(regionId, i);
		}

		player.getInventory().setItem(event.getSlot(), remaining.getAmount() > 0 ? remaining : null);
		StorageManager.saveStorage(regionId);
	}

	private void unregisterPassthrough() {
		InventoryManager.unregister(player);
		if (passthroughMenu != null) {
			passthroughMenu.destroy();
			passthroughMenu = null;
		}
	}

	private void cleanup() {
		if (!valid) return;
		valid = false;
		StorageManager.saveStorage(regionId);
		StorageManager.unregisterMenu(regionId, this);
		HandlerList.unregisterAll(this);
		unregisterPassthrough();
	}
}