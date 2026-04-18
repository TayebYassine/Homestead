package tfagaming.projects.minecraft.homestead.storage;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;

import java.util.UUID;

public class RegionStorage {

	public static boolean hasStorage(Region region) {
		return StorageManager.hasStorage(region.getUniqueId());
	}

	public static SharedStorage getStorage(Region region) {
		return StorageManager.getStorage(region.getUniqueId());
	}

	public static SharedStorage createStorage(Region region, int size) {
		return StorageManager.createStorage(region.getUniqueId(), size);
	}

	public static void deleteStorage(Region region) {
		StorageManager.deleteStorage(region.getUniqueId());
	}

	public static boolean canAccess(Region region, Player player) {
		if (region.isOwner(player.getUniqueId())) return true;
		if (region.isPlayerMember(player)) {
			return true;
		}
		return region.isPublic();
	}

	public static void openStorage(Region region, Player player) {
		if (!canAccess(region, player)) {
			player.sendMessage("§cNo access to this storage.");
			return;
		}

		SharedStorage storage = getStorage(region);

		String title = MenuUtils.getTitle(28).replace("{region}", region.getName());

		StorageMenu menu = new StorageMenu(player, region.getUniqueId(), title, storage.getSize());

		menu.addItem(getStorageSize(region) - 9, MenuUtils.getBackButton(), (_player, click) -> {
			if (click.isLeftClick()) {
				_player.closeInventory();
			}
		});

		menu.open();
	}

	public static int getStorageSize(Region region) {
		SharedStorage storage = StorageManager.getExistingStorage(region.getUniqueId());
		return storage != null ? storage.getSize() : 0;
	}

	public static boolean upgradeStorage(Region region, int newSize) {
		UUID regionId = region.getUniqueId();
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
}