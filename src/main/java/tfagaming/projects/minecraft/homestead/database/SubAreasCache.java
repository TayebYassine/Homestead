package tfagaming.projects.minecraft.homestead.database;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.SubArea;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SubAreasCache extends ConcurrentHashMap<UUID, SubArea> {
	public SubAreasCache(int interval) {
		Homestead.getInstance().runAsyncTimerTask(() -> {
			Homestead.database.exportSubAreas();
		}, 10, interval);
	}

	public List<SubArea> getAll() {
		return new ArrayList<>(this.values());
	}

	public void putOrUpdate(SubArea subArea) {
		this.put(subArea.getUniqueId(), subArea);
	}

	public long getLatency() {
		long before = System.currentTimeMillis();

		this.getAll();

		long after = System.currentTimeMillis();

		return after - before;
	}
}