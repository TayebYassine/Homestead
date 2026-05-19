package tfagaming.projects.minecraft.homestead.listeners.util;

import org.bukkit.Chunk;
import org.bukkit.entity.CopperGolem;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.models.Region;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CopperGolemTracker {

	private static final Map<UUID, Long> GOLEM_SPAWN_REGION = new ConcurrentHashMap<>();

	private CopperGolemTracker() {
	}

	public static void recordSpawnRegion(CopperGolem golem) {
		Chunk spawnChunk = golem.getLocation().getChunk();

		if (ChunkManager.isChunkClaimed(spawnChunk)) {
			Region region = ChunkManager.getRegionOwnsTheChunk(spawnChunk);
			if (region != null) {
				GOLEM_SPAWN_REGION.put(golem.getUniqueId(), region.getUniqueId());
			} else {
				GOLEM_SPAWN_REGION.put(golem.getUniqueId(), null);
			}
		} else {
			GOLEM_SPAWN_REGION.put(golem.getUniqueId(), null);
		}
	}

	public static Long getSpawnRegionId(CopperGolem golem) {
		return GOLEM_SPAWN_REGION.get(golem.getUniqueId());
	}

	public static void forgetGolem(CopperGolem golem) {
		GOLEM_SPAWN_REGION.remove(golem.getUniqueId());
	}
}