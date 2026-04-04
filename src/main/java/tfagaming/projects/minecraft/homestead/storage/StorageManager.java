package tfagaming.projects.minecraft.homestead.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {
	private static final Map<UUID, SharedStorage> storages = new ConcurrentHashMap<>();
	private static final Map<UUID, Set<StorageMenu>> activeMenus = new ConcurrentHashMap<>();
	private static Homestead plugin;
	private static File storageFile;
	private static FileConfiguration storageConfig;

	public static void init(Homestead homesteadPlugin) {
		plugin = homesteadPlugin;
		loadStorageFile();
	}

	private static void loadStorageFile() {
		storageFile = new File(plugin.getDataFolder(), "storages.yml");
		if (!storageFile.exists()) {
			try {
				storageFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		storageConfig = YamlConfiguration.loadConfiguration(storageFile);
	}

	public static SharedStorage getStorage(UUID regionId) {
		return storages.computeIfAbsent(regionId, id -> {
			String data = storageConfig.getString("storages." + id.toString());
			if (data != null && !data.isEmpty()) {
				try {
					return SharedStorage.deserialize(id, data);
				} catch (Exception e) {
					plugin.getLogger().warning("Failed to load storage for " + id);
				}
			}
			return SharedStorage.createEmpty(id);
		});
	}

	public static SharedStorage getExistingStorage(UUID regionId) {
		return storages.get(regionId);
	}

	public static boolean hasStorage(UUID regionId) {
		return storages.containsKey(regionId) || storageConfig.contains("storages." + regionId.toString());
	}

	public static SharedStorage createStorage(UUID regionId, int size) {
		SharedStorage storage = new SharedStorage(regionId, size);
		storages.put(regionId, storage);
		saveStorage(regionId);
		return storage;
	}

	public static void replaceStorage(UUID regionId, SharedStorage newStorage) {
		storages.put(regionId, newStorage);
		saveStorage(regionId);
		updateAllMenus(regionId);
	}

	public static void deleteStorage(UUID regionId) {
		storages.remove(regionId);
		storageConfig.set("storages." + regionId.toString(), null);
		saveConfig();
		closeAllMenus(regionId);
	}

	public static void saveStorage(UUID regionId) {
		SharedStorage storage = storages.get(regionId);
		if (storage != null) {
			storageConfig.set("storages." + regionId.toString(), storage.serialize());
			saveConfig();
		}
	}

	public static void saveAll() {
		for (Map.Entry<UUID, SharedStorage> entry : storages.entrySet()) {
			storageConfig.set("storages." + entry.getKey().toString(), entry.getValue().serialize());
		}
		saveConfig();
	}

	private static void saveConfig() {
		plugin.runAsyncTask(() -> {
			try {
				storageConfig.save(storageFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public static void registerMenu(UUID regionId, StorageMenu menu) {
		activeMenus.computeIfAbsent(regionId, k -> ConcurrentHashMap.newKeySet()).add(menu);
	}

	public static void unregisterMenu(UUID regionId, StorageMenu menu) {
		Set<StorageMenu> menus = activeMenus.get(regionId);
		if (menus != null) {
			menus.remove(menu);
			if (menus.isEmpty()) activeMenus.remove(regionId);
		}
	}

	public static void updateAllMenus(UUID regionId) {
		Set<StorageMenu> menus = activeMenus.get(regionId);
		if (menus == null || menus.isEmpty()) return;
		SharedStorage storage = getStorage(regionId);
		plugin.runSyncTask(() -> {
			for (StorageMenu menu : menus) {
				if (menu.isValid()) menu.refreshDisplay();
			}
		});
	}

	public static void updateSlot(UUID regionId, int slot) {
		Set<StorageMenu> menus = activeMenus.get(regionId);
		if (menus == null || menus.isEmpty()) return;
		SharedStorage storage = getStorage(regionId);
		ItemStack item = storage.getItem(slot);
		plugin.runSyncTask(() -> {
			for (StorageMenu menu : menus) {
				if (menu.isValid()) menu.updateSlot(slot, item);
			}
		});
	}

	public static void closeAllMenus(UUID regionId) {
		Set<StorageMenu> menus = activeMenus.remove(regionId);
		if (menus != null) {
			for (StorageMenu menu : new ArrayList<>(menus)) {
				menu.forceClose();
			}
		}
	}

	public static void closePlayerMenus(Player player) {
		for (Iterator<Map.Entry<UUID, Set<StorageMenu>>> it = activeMenus.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<UUID, Set<StorageMenu>> entry = it.next();
			Set<StorageMenu> menus = entry.getValue();
			for (Iterator<StorageMenu> menuIt = menus.iterator(); menuIt.hasNext(); ) {
				StorageMenu menu = menuIt.next();
				if (menu.getPlayer().equals(player)) {
					menu.forceClose();
					menuIt.remove();
				}
			}
			if (menus.isEmpty()) it.remove();
		}
	}
}