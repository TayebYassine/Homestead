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
	private static final Map<Long, SharedStorage> STORAGES = new ConcurrentHashMap<>();
	private static final Map<Long, Set<StorageMenu>> ACTIVE_MENUS = new ConcurrentHashMap<>();
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

	public static SharedStorage getStorage(long regionId) {
		return STORAGES.computeIfAbsent(regionId, id -> {
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

	public static SharedStorage getExistingStorage(long regionId) {
		return STORAGES.get(regionId);
	}

	public static boolean hasStorage(long regionId) {
		return STORAGES.containsKey(regionId) || storageConfig.contains("storages." + regionId);
	}

	public static SharedStorage createStorage(long regionId, int size) {
		SharedStorage storage = new SharedStorage(regionId, size);
		STORAGES.put(regionId, storage);
		saveStorage(regionId);
		return storage;
	}

	public static void replaceStorage(long regionId, SharedStorage newStorage) {
		STORAGES.put(regionId, newStorage);
		saveStorage(regionId);
		updateAllMenus(regionId);
	}

	public static void deleteStorage(long regionId) {
		STORAGES.remove(regionId);
		storageConfig.set("storages." + regionId, null);
		saveConfig();
		closeAllMenus(regionId);
	}

	public static void saveStorage(long regionId) {
		SharedStorage storage = STORAGES.get(regionId);
		if (storage != null) {
			storageConfig.set("storages." + regionId, storage.serialize());
			plugin.runAsyncTask(StorageManager::saveConfig);
		}
	}

	public static void saveAll() {
		for (Map.Entry<Long, SharedStorage> entry : STORAGES.entrySet()) {
			storageConfig.set("storages." + entry.getKey().toString(), entry.getValue().serialize());
		}
		saveConfig();
	}

	private static void saveConfig() {
		try {
			storageConfig.save(storageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void registerMenu(long regionId, StorageMenu menu) {
		ACTIVE_MENUS.computeIfAbsent(regionId, k -> ConcurrentHashMap.newKeySet()).add(menu);
	}

	public static void unregisterMenu(long regionId, StorageMenu menu) {
		Set<StorageMenu> menus = ACTIVE_MENUS.get(regionId);
		if (menus != null) {
			menus.remove(menu);
			if (menus.isEmpty()) ACTIVE_MENUS.remove(regionId);
		}
	}

	public static void updateAllMenus(long regionId) {
		Set<StorageMenu> menus = ACTIVE_MENUS.get(regionId);
		if (menus == null || menus.isEmpty()) return;
		List<StorageMenu> snapshot = new ArrayList<>(menus);
		plugin.runSyncTask(() -> {
			for (StorageMenu menu : snapshot) {
				if (menu.isValid()) menu.refreshDisplay();
			}
		});
	}

	public static void updateSlot(long regionId, int slot) {
		Set<StorageMenu> menus = ACTIVE_MENUS.get(regionId);
		if (menus == null || menus.isEmpty()) return;
		List<StorageMenu> snapshot = new ArrayList<>(menus);
		plugin.runSyncTask(() -> {
			SharedStorage storage = getStorage(regionId);
			ItemStack item = storage.getItem(slot);
			for (StorageMenu menu : snapshot) {
				if (menu.isValid()) menu.updateSlot(slot, item);
			}
		});
	}

	public static void closeAllMenus(long regionId) {
		Set<StorageMenu> menus = ACTIVE_MENUS.remove(regionId);
		if (menus != null) {
			for (StorageMenu menu : new ArrayList<>(menus)) {
				menu.forceClose();
			}
		}
	}

	public static void closePlayerMenus(Player player) {
		for (Iterator<Map.Entry<Long, Set<StorageMenu>>> it = ACTIVE_MENUS.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Long, Set<StorageMenu>> entry = it.next();
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