package tfagaming.projects.minecraft.homestead.gui;

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
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Menu implements Listener {
	private final Homestead plugin;
	private final Inventory inventory;
	private final Map<Integer, BiConsumer<Player, InventoryClickEvent>> callbacks;
	private boolean passthrough = false;

	/**
	 * Menu constructor.
	 * @param title The menu title
	 * @param size The size of the menu
	 */
	public Menu(String title, int size) {
		this.plugin = Homestead.getInstance();
		this.inventory = Bukkit.createInventory(null, size, ColorTranslator.translate(title));
		this.callbacks = new HashMap<>();

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * When {@code true}, any click event will be ignored, allowing players to
	 * move items freely from the menu to their inventory.
	 * @param passthrough Passthrough value
	 */
	public Menu setPassthrough(boolean passthrough) {
		this.passthrough = passthrough;
		return this;
	}

	/**
	 * Add an item or a button to the menu.
	 * @param slot The slot index
	 * @param itemStack The item stack
	 * @param callback Action to perform once the item was clicked by a player
	 */
	public void addItem(int slot, ItemStack itemStack, @Nullable BiConsumer<Player, InventoryClickEvent> callback) {
		if (slot < 0 || slot >= inventory.getSize()) {
			return;
		}

		inventory.setItem(slot, itemStack);

		if (callback != null) {
			callbacks.put(slot, callback);
		}
	}

	/**
	 * Open the menu for a player.
	 * @param player The player
	 */
	public void open(Player player) {
		player.openInventory(inventory);

		InventoryManager.register(player, this);
	}

	/**
	 * Open the menu for a player and fill empty slots with an item stack.
	 * @param player The player
	 * @param filler The item stack filler
	 */
	public void open(Player player, ItemStack filler) {
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) == null) {
				inventory.setItem(i, filler);
			}
		}

		player.openInventory(inventory);

		InventoryManager.register(player, this);
	}

	/**
	 * Unregister the menu instance from the listener.
	 */
	public void destroy() {
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) {
			return;
		}

		if (InventoryManager.getMenu(player) == this && event.getInventory().equals(this.inventory)) {
			if (passthrough) return;

			event.setCancelled(true);
			if (event.getClick() == ClickType.MIDDLE) return;

			int slot = event.getRawSlot();

			if (callbacks.containsKey(slot) && callbacks.get(slot) != null) {
				plugin.runPlayerTask(player, () -> {
					callbacks.get(slot).accept(player, event);
				});
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();

		if (InventoryManager.getMenu(player) == this) {
			InventoryManager.unregister(player);
			destroy();
		}
	}
}