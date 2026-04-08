package tfagaming.projects.minecraft.homestead.integrations;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import tfagaming.projects.minecraft.homestead.logs.Logger;

public final class WorldGuardAPI {
	public static boolean isChunkInRegion(Chunk chunk) {
		if (!isAvailable()) {
			Logger.warning(Logger.PredefinedMessages.WORLDGUARD_PLUGIN_NOT_FOUND.getMessage());
			return false;
		}

		try {
			RegionContainer regionContainer = getInstance().getPlatform().getRegionContainer();

			com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(chunk.getWorld());

			if (worldEditWorld == null) {
				return false;
			}

			RegionManager regionManager = regionContainer.get(worldEditWorld);
			if (regionManager == null) {
				return false;
			}

			int chunkX = chunk.getX() << 4;
			int chunkZ = chunk.getZ() << 4;

			for (int x = chunkX; x < chunkX + 16; x++) {
				for (int z = chunkZ; z < chunkZ + 16; z++) {
					Location location = new Location(chunk.getWorld(), x, 64, z);
					com.sk89q.worldedit.math.BlockVector3 blockVector = BukkitAdapter.asBlockVector(location);

					ApplicableRegionSet regionSet = regionManager.getApplicableRegions(blockVector);

					if (regionSet.size() > 0) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}

		return false;
	}

	public static boolean isAvailable() {
		try {
			Class.forName("com.sk89q.worldguard.WorldGuard");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static com.sk89q.worldguard.WorldGuard getInstance() {
		return com.sk89q.worldguard.WorldGuard.getInstance();
	}
}
