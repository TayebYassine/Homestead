package me.tayebyassine.homestead.storage;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.logs.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for all region storages.
 *
 * <p>Handles loading, saving, and caching of {@link SharedStorage} instances,
 * and coordinates open {@link StorageMenu} views so they stay synchronised
 * when the underlying storage changes.
 * </p>
 */
public final class StorageManager {

    private static final Map<Long, SharedStorage> STORAGES = new ConcurrentHashMap<>();
    private static final Map<Long, Set<StorageMenu>> ACTIVE_MENUS = new ConcurrentHashMap<>();
    private static Homestead plugin;
    private static File storageFile;
    private static FileConfiguration storageConfig;

    private StorageManager() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Initialize the storage manager with the plugin instance and load
     * the persisted storage file.
     *
     * @param homesteadPlugin the plugin instance
     */
    public static void init(Homestead homesteadPlugin) {
        plugin = homesteadPlugin;
        loadStorageFile();
    }

    /**
     * Load or create the {@code storages.yml} file and parse its contents
     * into a {@link FileConfiguration}.
     */
    private static void loadStorageFile() {
        storageFile = new File(plugin.getDataFolder(), "storages.yml");
        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                Logger.error("Failed to create a new storage");
                Logger.error(e);
            }
        }
        storageConfig = YamlConfiguration.loadConfiguration(storageFile);
    }

    /**
     * Get the storage for the given region, creating it from the persisted
     * file if it is not yet cached.
     *
     * @param regionId the unique region ID
     * @return the shared storage instance, never {@code null}
     */
    public static SharedStorage getStorage(long regionId) {
        return STORAGES.computeIfAbsent(regionId, id -> {
            String data = storageConfig.getString("storages." + id);
            if (data != null && !data.isEmpty()) {
                try {
                    return SharedStorage.deserialize(id, data);
                } catch (Exception e) {
                    Logger.error("Failed to load storage for " + id);
                    Logger.error(e);
                }
            }
            return SharedStorage.createEmpty(id);
        });
    }

    /**
     * Get the cached storage for the given region without loading from
     * disk.
     *
     * @param regionId the unique region ID
     * @return the cached storage, or {@code null} if not yet loaded
     */
    public static SharedStorage getExistingStorage(long regionId) {
        return STORAGES.get(regionId);
    }

    /**
     * Check whether a storage exists for the given region, either in the
     * in-memory cache or on disk.
     *
     * @param regionId the unique region ID
     * @return {@code true} if a storage is present
     */
    public static boolean hasStorage(long regionId) {
        return STORAGES.containsKey(regionId) || (storageConfig != null && storageConfig.contains("storages." + regionId));
    }

    /**
     * Create a new empty storage for the given region and persist it.
     *
     * @param regionId the unique region ID
     * @param size     the inventory size (must be a valid Bukkit size)
     * @return the newly created storage
     */
    public static SharedStorage createStorage(long regionId, int size) {
        SharedStorage storage = new SharedStorage(regionId, size);
        STORAGES.put(regionId, storage);
        saveStorage(regionId);
        return storage;
    }

    /**
     * Replace the storage for the given region with a new instance and
     * refresh all open menus.
     *
     * @param regionId   the unique region ID
     * @param newStorage the replacement storage
     */
    public static void replaceStorage(long regionId, SharedStorage newStorage) {
        STORAGES.put(regionId, newStorage);
        saveStorage(regionId);
        updateAllMenus(regionId);
    }

    /**
     * Delete the storage for the given region from both the cache and the
     * persisted file, and close all open menus.
     *
     * @param regionId the unique region ID
     */
    public static void deleteStorage(long regionId) {
        if (storageConfig == null) {
            return;
        }

        STORAGES.remove(regionId);
        storageConfig.set("storages." + regionId, null);
        saveConfig();
        closeAllMenus(regionId);
    }

    /**
     * Persist the storage for the given region to disk asynchronously.
     *
     * @param regionId the unique region ID
     */
    public static void saveStorage(long regionId) {
        if (storageConfig == null) {
            return;
        }

        SharedStorage storage = STORAGES.get(regionId);
        if (storage != null) {
            storageConfig.set("storages." + regionId, storage.serialize());
            plugin.runAsyncTask(StorageManager::saveConfig);
        }
    }

    /**
     * Persist all cached storages to disk synchronously.
     *
     * <p>Called during plugin shutdown to ensure no data is lost.
     * </p>
     */
    public static void saveAll() {
        if (storageConfig == null) {
            return;
        }

        for (Map.Entry<Long, SharedStorage> entry : STORAGES.entrySet()) {
            storageConfig.set("storages." + entry.getKey().toString(), entry.getValue().serialize());
        }
        saveConfig();
    }

    /**
     * Write the in-memory {@link FileConfiguration} to the
     * {@code storages.yml} file.
     */
    private static void saveConfig() {
        if (storageConfig == null) {
            return;
        }

        try {
            storageConfig.save(storageFile);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    /**
     * Register an open {@link StorageMenu} so it receives updates when the
     * underlying storage changes.
     *
     * @param regionId the unique region ID
     * @param menu     the menu to register
     */
    public static void registerMenu(long regionId, StorageMenu menu) {
        ACTIVE_MENUS.computeIfAbsent(regionId, k -> ConcurrentHashMap.newKeySet()).add(menu);
    }

    /**
     * Unregister a {@link StorageMenu} so it no longer receives updates.
     *
     * @param regionId the unique region ID
     * @param menu     the menu to unregister
     */
    public static void unregisterMenu(long regionId, StorageMenu menu) {
        Set<StorageMenu> menus = ACTIVE_MENUS.get(regionId);
        if (menus != null) {
            menus.remove(menu);
            if (menus.isEmpty()) ACTIVE_MENUS.remove(regionId);
        }
    }

    /**
     * Refresh the full display of all open menus for the given region.
     *
     * @param regionId the unique region ID
     */
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

    /**
     * Update a single slot across all open menus for the given region.
     *
     * @param regionId the unique region ID
     * @param slot     the inventory slot index
     */
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

    /**
     * Force-close all open menus for the given region. Typically called
     * when the storage is being deleted.
     *
     * @param regionId the unique region ID
     */
    public static void closeAllMenus(long regionId) {
        Set<StorageMenu> menus = ACTIVE_MENUS.remove(regionId);
        if (menus != null) {
            for (StorageMenu menu : new ArrayList<>(menus)) {
                menu.forceClose();
            }
        }
    }

    /**
     * Force-close all menus owned by the given player. Called when the
     * player disconnects.
     *
     * @param player the player whose menus to close
     */
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
