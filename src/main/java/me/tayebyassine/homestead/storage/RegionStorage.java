package me.tayebyassine.homestead.storage;

import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.minecraft.menus.MenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * High-level, region-aware facade for the storage system.
 */
public final class RegionStorage {

    private RegionStorage() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Check whether the given region has a storage.
     *
     * @param region the region to check
     * @return {@code true} if a storage exists for the region
     */
    public static boolean hasStorage(Region region) {
        return StorageManager.hasStorage(region.getUniqueId());
    }

    /**
     * Get the storage for the given region, loading it from disk if
     * necessary.
     *
     * @param region the region whose storage to retrieve
     * @return the shared storage instance
     */
    public static SharedStorage getStorage(Region region) {
        return StorageManager.getStorage(region.getUniqueId());
    }

    /**
     * Get the current storage size (slot count) for the given region.
     *
     * @param region the region to query
     * @return the storage size, or {@code 0} if no storage exists
     */
    public static int getStorageSize(Region region) {
        SharedStorage storage = StorageManager.getExistingStorage(region.getUniqueId());
        return storage != null ? storage.getSize() : 0;
    }

    /**
     * Create a new storage for the given region.
     *
     * @param region the region to create storage for
     * @param size   the inventory size (must be a valid Bukkit size)
     * @return the newly created storage
     */
    public static SharedStorage createStorage(Region region, int size) {
        return StorageManager.createStorage(region.getUniqueId(), size);
    }

    /**
     * Delete the storage for the given region.
     *
     * @param region the region whose storage to delete
     */
    public static void deleteStorage(Region region) {
        StorageManager.deleteStorage(region.getUniqueId());
    }

    /**
     * Upgrade the storage for the given region to a larger size. Existing
     * items are copied to the new storage.
     *
     * @param region  the region whose storage to upgrade
     * @param newSize the new inventory size
     * @return {@code true} if the upgrade was performed, {@code false} if
     * no upgrade was needed or no storage exists
     */
    public static boolean upgradeStorage(Region region, int newSize) {
        long regionId = region.getUniqueId();
        SharedStorage existing = StorageManager.getExistingStorage(regionId);

        if (existing == null) {
            StorageManager.createStorage(regionId, newSize);
            return true;
        }
        if (newSize <= existing.getSize()) return false;

        SharedStorage newStorage = new SharedStorage(regionId, newSize);
        for (int i = 0; i < existing.getSize(); i++) {
            ItemStack item = existing.getItem(i);
            if (item != null) newStorage.setItem(i, item);
        }

        StorageManager.replaceStorage(regionId, newStorage);
        return true;
    }

    /**
     * Check whether the given player can access the region's storage.
     *
     * @param region the region
     * @param player the player to check
     * @return {@code true} if the player is the owner, a member, or the
     * region is public
     */
    public static boolean canAccess(Region region, Player player) {
        if (region.isOwner(player.getUniqueId())) return true;
        if (MemberManager.isMemberOfRegion(region, player)) {
            return true;
        }
        return region.isPublic();
    }

    /**
     * Open the region's storage for the given player. The player must
     * have access via {@link #canAccess(Region, Player)}.
     *
     * @param region the region whose storage to open
     * @param player the player to open the storage for
     */
    public static void openStorage(Region region, Player player) {
        if (!canAccess(region, player)) {
            return;
        }

        SharedStorage storage = getStorage(region);

        String title = MenuUtility.getTitle(28).replace("{region}", region.getName());

        StorageMenu menu = new StorageMenu(player, region.getUniqueId(), title, storage.getSize());

        menu.addItem(getStorageSize(region) - 9, MenuUtility.getBackButton(), (_player, click) -> {
            if (click.isLeftClick()) {
                _player.closeInventory();
            }
        });

        menu.open();
    }
}
