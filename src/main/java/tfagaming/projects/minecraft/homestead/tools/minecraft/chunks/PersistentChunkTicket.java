package tfagaming.projects.minecraft.homestead.tools.minecraft.chunks;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PersistentChunkTicket {
	private PersistentChunkTicket() {
	}

	public static void addPersistent(Homestead plugin, Chunk chunk) {
		World world = chunk.getWorld();

		if (world == null) {
			return;
		}

		addPersistent(plugin, world, chunk.getX(), chunk.getZ());
	}

	public static void addPersistent(Homestead plugin, World world, int chunkX, int chunkZ) {
		world.addPluginChunkTicket(chunkX, chunkZ, plugin);
	}

	public static void removePersistent(Homestead plugin, Chunk chunk) {
		World world = chunk.getWorld();

		if (world == null) {
			return;
		}

		removePersistent(plugin, world, chunk.getX(), chunk.getZ());
	}

	public static void removePersistent(Homestead plugin, World world, int chunkX, int chunkZ) {
		world.removePluginChunkTicket(chunkX, chunkZ, plugin);
	}

	public static List<Chunk> getAllForceLoadedChunks() {
		List<Chunk> result = new ArrayList<>();

		for (World world : Bukkit.getWorlds()) {
			Collection<Chunk> pluginChunks = world.getPluginChunkTickets()
					.getOrDefault(Homestead.getInstance(), Collections.emptyList());

			result.addAll(pluginChunks);
		}

		return result;
	}
}
