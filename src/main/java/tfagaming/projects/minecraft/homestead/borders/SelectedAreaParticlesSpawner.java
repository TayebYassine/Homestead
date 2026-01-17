package tfagaming.projects.minecraft.homestead.borders;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectedAreaParticlesSpawner {
	private static final Map<UUID, BukkitTask> tasks = new HashMap<>();

	private final Player player;
	private final SerializableBlock firstBlock;
	private final SerializableBlock secondBlock;

	public SelectedAreaParticlesSpawner(Player player, Block firstBlock, Block secondBlock) {
		this.player = player;
		this.firstBlock = new SerializableBlock(firstBlock);
		this.secondBlock = new SerializableBlock(secondBlock);

		boolean isEnabled = Homestead.config.get("borders.enabled");

		if (isEnabled) {
			if (tasks.containsKey(player.getUniqueId())) {
				BukkitTask taskFromMap = tasks.get(player.getUniqueId());

				cancelTask(taskFromMap, player);
			}

			startRepeatingEffect(15L);
		}
	}

	public SelectedAreaParticlesSpawner(Player player, SerializableBlock firstBlock, SerializableBlock secondBlock) {
		this.player = player;
		this.firstBlock = firstBlock;
		this.secondBlock = secondBlock;

		boolean isEnabled = Homestead.config.get("borders.enabled");

		if (isEnabled) {
			if (tasks.containsKey(player.getUniqueId())) {
				BukkitTask taskFromMap = tasks.get(player.getUniqueId());

				cancelTask(taskFromMap, player);
			}

			startRepeatingEffect(15L);
		}
	}

	public static void cancelTask(BukkitTask task, Player player) {
		if (task != null) {
			tasks.remove(player.getUniqueId());

			task.cancel();
			task = null;
		}
	}

	public static void cancelTask(Player player) {
		BukkitTask task = tasks.get(player.getUniqueId());

		if (task != null) {
			tasks.remove(player.getUniqueId());

			task.cancel();
			task = null;
		}
	}

	public void spawnParticles() {
		int minX = Math.min(firstBlock.getX(), secondBlock.getX());
		int minY = Math.min(firstBlock.getY(), secondBlock.getY());
		int minZ = Math.min(firstBlock.getZ(), secondBlock.getZ());
		int maxX = Math.max(firstBlock.getX(), secondBlock.getX());
		int maxY = Math.max(firstBlock.getY(), secondBlock.getY());
		int maxZ = Math.max(firstBlock.getZ(), secondBlock.getZ());

		int step = 1;

		for (int x = minX; x <= maxX; x += step) {
			spawnDustParticle(x, minY, minZ); // Bottom front
			spawnDustParticle(x, minY, maxZ); // Bottom back
			spawnDustParticle(x, maxY, minZ); // Top front
			spawnDustParticle(x, maxY, maxZ); // Top back
		}

		for (int y = minY; y <= maxY; y += step) {
			spawnDustParticle(minX, y, minZ); // Left front
			spawnDustParticle(minX, y, maxZ); // Left back
			spawnDustParticle(maxX, y, minZ); // Right front
			spawnDustParticle(maxX, y, maxZ); // Right back
		}

		for (int z = minZ; z <= maxZ; z += step) {
			spawnDustParticle(minX, minY, z); // Bottom left
			spawnDustParticle(maxX, minY, z); // Bottom right
			spawnDustParticle(minX, maxY, z); // Top left
			spawnDustParticle(maxX, maxY, z); // Top right
		}
	}

	private void spawnDustParticle(int x, int y, int z) {
		Location location = new Location(player.getWorld(), x + 0.5, y + 0.5, z + 0.5);

		DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 179, 255), 2.0F);

		player.spawnParticle(Particle.DUST, location, 5, dustOptions);
	}

	public void startRepeatingEffect(long intervalTicks) {
		Homestead instance = Homestead.getInstance();

		BukkitTask task = instance.runAsyncTimerTask(this::spawnParticles, 1);

		tasks.put(player.getUniqueId(), task);

		// Automatically cancel task  60 seconds
		instance.runAsyncTaskLater(() -> {
			cancelTask(task, player);
		}, 60);
	}
}
