package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.War;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WarsCache extends ConcurrentHashMap<UUID, War> {
	public WarsCache(int interval) {
		Homestead.getInstance().runAsyncTimerTask(() -> {
			Homestead.database.exportWars();
		}, 10, interval);
	}

	public List<War> getAll() {
		return new ArrayList<>(this.values());
	}

	public void putOrUpdate(War war) {
		this.put(war.getUniqueId(), war);
	}

	public long getLatency() {
		long before = System.currentTimeMillis();

		this.getAll();

		long after = System.currentTimeMillis();

		return after - before;
	}
}