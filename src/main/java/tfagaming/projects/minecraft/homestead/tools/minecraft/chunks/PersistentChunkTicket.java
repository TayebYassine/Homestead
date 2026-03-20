package tfagaming.projects.minecraft.homestead.tools.minecraft.chunks;

import org.bukkit.Chunk;
import org.bukkit.World;
import tfagaming.projects.minecraft.homestead.Homestead;

public class PersistentChunkTicket {
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
}
