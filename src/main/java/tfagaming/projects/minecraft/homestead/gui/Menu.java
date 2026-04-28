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
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Menu implements Listener {

	protected final Homestead plugin;
	protected final Inventory inventory;
	protected final Map<Integer, BiConsumer<Player, InventoryClickEvent>> callbacks = new HashMap<>();
	protected boolean passthrough = false;

	protected Menu(Builder<?> builder) {
		this.plugin = Homestead.getInstance();
		this.inventory = Bukkit.createInventory(null, builder.size, ColorTranslator.translate(builder.title));
		this.passthrough = builder.passthrough;

		for (Map.Entry<Integer, ItemStack> entry : builder.items.entrySet()) {
			inventory.setItem(entry.getKey(), entry.getValue());
		}

		for (Map.Entry<Integer, BiConsumer<Player, InventoryClickEvent>> entry : builder.buttons.entrySet()) {
			callbacks.put(entry.getKey(), entry.getValue());
		}

		if (builder.filler != null) {
			for (int i = 0; i < inventory.getSize(); i++) {
				if (inventory.getItem(i) == null) {
					inventory.setItem(i, builder.filler);
				}
			}
		}

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public Menu setPassthrough(boolean passthrough) {
		this.passthrough = passthrough;
		return this;
	}

	public Menu setItem(int slot, ItemStack item) {
		if (isValidSlot(slot)) {
			inventory.setItem(slot, item);
		}
		return this;
	}

	public Menu setButton(int slot, ItemStack item, BiConsumer<Player, InventoryClickEvent> callback) {
		if (!isValidSlot(slot)) return this;

		inventory.setItem(slot, item);
		if (callback != null) {
			callbacks.put(slot, callback);
		}
		return this;
	}

	public Menu fillEmptySlots(ItemStack filler) {
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) == null) {
				inventory.setItem(i, filler);
			}
		}
		return this;
	}

	public Menu fillEmptySlots() {
		return fillEmptySlots(MenuUtility.getEmptySlot());
	}

	public void open(Player player) {
		player.openInventory(inventory);
		InventoryManager.register(player, this);
	}

	public void destroy() {
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) return;
		if (InventoryManager.getMenu(player) != this) return;
		if (!event.getInventory().equals(this.inventory)) return;

		if (InventoryManager.isOnCooldown(player)) {
			event.setCancelled(true);
			return;
		}
		InventoryManager.updateCooldown(player);

		if (passthrough) return;

		event.setCancelled(true);
		if (event.getClick() == ClickType.MIDDLE) return;

		int slot = event.getRawSlot();
		BiConsumer<Player, InventoryClickEvent> action = callbacks.get(slot);
		if (action != null) {
			plugin.runPlayerTask(player, () -> action.accept(player, event));
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

	protected boolean isValidSlot(int slot) {
		return slot >= 0 && slot < inventory.getSize();
	}

	public static Builder<?> builder(int pathTitle, int size) {
		return new Builder<>(pathTitle, size);
	}

	public static Builder<?> builder(String title, int size) {
		return new Builder<>(title, size);
	}

	public static class Builder<T extends Builder<T>> {
		protected final String title;
		protected final int size;
		protected boolean passthrough = false;
		protected final Map<Integer, ItemStack> items = new HashMap<>();
		protected final Map<Integer, BiConsumer<Player, InventoryClickEvent>> buttons = new HashMap<>();
		protected ItemStack filler;

		protected Builder(int pathTitle, int size) {
			this(MenuUtility.getTitle(pathTitle), size);
		}

		protected Builder(String title, int size) {
			if (size % 9 != 0 || size < 9 || size > 54) {
				throw new IllegalArgumentException("Inventory size must be a multiple of 9 between 9 and 54.");
			}
			this.title = title;
			this.size = size;
		}

		@SuppressWarnings("unchecked")
		protected T self() {
			return (T) this;
		}

		public T passthrough(boolean passthrough) {
			this.passthrough = passthrough;
			return self();
		}

		public T item(int slot, ItemStack item) {
			if (slot >= 0 && slot < size) {
				items.put(slot, item);
			}
			return self();
		}

		public T button(int slot, ItemStack item, BiConsumer<Player, InventoryClickEvent> callback) {
			if (slot >= 0 && slot < size) {
				items.put(slot, item);
				if (callback != null) {
					buttons.put(slot, callback);
				}
			}
			return self();
		}

		public T fillEmptySlots(ItemStack filler) {
			this.filler = filler;
			return self();
		}

		public T fillEmptySlots() {
			return fillEmptySlots(MenuUtility.getEmptySlot());
		}

		public Menu build() {
			return new Menu(this);
		}
	}
}