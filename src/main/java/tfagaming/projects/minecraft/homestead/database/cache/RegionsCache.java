package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RegionsCache extends ConcurrentHashMap<UUID, Region> {
	public RegionsCache(int interval) {
		Homestead.getInstance().runAsyncTimerTask(() -> {
			Homestead.database.exportRegions();
		}, 10, interval);
	}

	public List<Region> getAll() {
		return new ArrayList<>(this.values());
	}

	public void putOrUpdate(Region region) {
		this.put(region.getUniqueId(), region);
	}

	public long getLatency() {
		long before = System.currentTimeMillis();

		this.getAll();

		long after = System.currentTimeMillis();

		return after - before;
	}
}
