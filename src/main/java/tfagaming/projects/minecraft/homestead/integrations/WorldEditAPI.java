package tfagaming.projects.minecraft.homestead.integrations;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import tfagaming.projects.minecraft.homestead.Homestead;

public final class WorldEditAPI {
	/**
	 * Regenerate chunks with WorldEdit API.
	 * @deprecated Use {@link FastAsyncWorldEditAPI} instead.
	 * @param world The chunk world
	 * @param chunk The chunk
	 */
	public static void regenerateChunk(World world, Chunk chunk) {
		regenerateChunk(world, chunk.getX(), chunk.getZ());
	}

	/**
	 * Regenerate chunks with WorldEdit API.
	 * @deprecated Use {@link FastAsyncWorldEditAPI} instead.
	 * @param world The chunk world
	 * @param chunkX The chunk X
	 * @param chunkZ The chunk Z
	 */
	public static void regenerateChunk(World world, int chunkX, int chunkZ) {
		Bukkit.getScheduler().runTaskAsynchronously(Homestead.getInstance(), () -> {
			com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
			CuboidRegion region = new CuboidRegion(BlockVector3.at(chunkX << 4, weWorld.getMinY(), chunkZ << 4),
					BlockVector3.at((chunkX << 4) + 15, weWorld.getMaxY(), (chunkZ << 4) + 15));
			WorldEdit worldEdit = WorldEdit.getInstance();

			@SuppressWarnings("deprecation")
			EditSession editSession = worldEdit.getEditSessionFactory().getEditSession(weWorld, -1);
			weWorld.regenerate(region, editSession);

			editSession.close();
		});
	}
}
