package tfagaming.projects.minecraft.homestead.integrations;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Chunk;
import org.bukkit.World;
import tfagaming.projects.minecraft.homestead.logs.Logger;

public class FastAsyncWorldEditAPI {
	/**
	 * Regenerate chunks with FastAsyncWorldEdit API.
	 * @param world The chunk world
	 * @param chunk The chunk
	 */
	public static void regenerateChunk(World world, Chunk chunk) {
		regenerateChunk(world, chunk.getX(), chunk.getZ());
	}

	/**
	 * Regenerate chunks with FastAsyncWorldEdit API.
	 * @param world The chunk world
	 * @param chunkX The chunk X
	 * @param chunkZ The chunk Z
	 */
	public static void regenerateChunk(World world, int chunkX, int chunkZ) {
		if (!isAvailable()) {
			Logger.debug("[FastAsyncWorldEdit] Attempted to regenerate a chunk, but FastAsyncWorldEdit class was not found.");
			return;
		}

		try {
			com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);

			int minX = chunkX << 4;
			int minZ = chunkZ << 4;
			int maxX = minX + 15;
			int maxZ = minZ + 15;

			CuboidRegion region = new CuboidRegion(
					BlockVector3.at(minX, weWorld.getMinY(), minZ),
					BlockVector3.at(maxX, weWorld.getMaxY(), maxZ)
			);

			WorldEdit worldEdit = WorldEdit.getInstance();

			EditSession editSession = worldEdit.getEditSessionFactory().getEditSession(weWorld, -1);

			weWorld.regenerate(region, editSession);

			editSession.close();

			Logger.debug("[FastAsyncWorldEdit] Successfully regenerated chunk at", chunkX, ",", chunkZ);
		} catch (Exception e) {
			Logger.debug("[FastAsyncWorldEdit] Failed to regenerate chunk at", chunkX, ",", chunkZ, ":", e.getMessage());
		}
	}

	public static boolean isAvailable() {
		try {
			Class.forName("com.fastasyncworldedit.core.FaweAPI");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
