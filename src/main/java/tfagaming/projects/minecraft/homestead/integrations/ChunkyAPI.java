package tfagaming.projects.minecraft.homestead.integrations;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import tfagaming.projects.minecraft.homestead.logs.Logger;

public final class ChunkyAPI {
	/**
	 * Regenerate chunks with Chunky API.
	 * @param world The chunk world
	 * @param chunk The chunk
	 */
	public static void regenerateChunk(World world, Chunk chunk) {
		regenerateChunk(world, chunk.getX(), chunk.getZ());
	}

	/**
	 * Regenerate chunks with Chunky API.
	 * @param world The chunk world
	 * @param chunkX The chunk X
	 * @param chunkZ the chunk Z
	 */
	public static void regenerateChunk(World world, int chunkX, int chunkZ) {
		if (getAPI() == null) {
			Logger.debug("ChunkyAPI not found, skipping chunk regeneration");
			return;
		}

		org.popcraft.chunky.api.ChunkyAPI chunky = getAPI();

		double centerX = chunkX * 16 + 8;
		double centerZ = chunkZ * 16 + 8;

		double radius = 8;

		String worldName = world.getName();

		chunky.onGenerationComplete(event -> {
			if (event.world().equals(worldName)) {
				Logger.debug("Chunk regeneration complete, world:", worldName);
			}
		});

		boolean started = chunky.startTask(
				worldName,
				"square",
				centerX,
				centerZ,
				radius,
				radius,
				"concentric"
		);

		if (started) {
			Logger.debug("Started chunk regeneration at chunk", chunkX, ",", chunkZ, "(block coords:", centerX, ",", centerZ + ")");
		} else {
			Logger.debug("Failed to start chunk regeneration, task may already be running");
		}
	}

	/**
	 * Returns {@link org.popcraft.chunky.api.ChunkyAPI} when found, {@code null} otherwise.
	 */
	private static org.popcraft.chunky.api.ChunkyAPI getAPI() {
		try {
			Class.forName("org.popcraft.chunky.api.ChunkyAPI");

			return Bukkit.getServer().getServicesManager().load(org.popcraft.chunky.api.ChunkyAPI.class);
		} catch (NoClassDefFoundError | ClassNotFoundException e) {
			return null;
		}
	}
}
