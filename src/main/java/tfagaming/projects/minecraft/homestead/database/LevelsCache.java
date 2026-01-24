package tfagaming.projects.minecraft.homestead.database;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LevelsCache extends ConcurrentHashMap<UUID, Level> {
	public LevelsCache(int interval) {
		Homestead.getInstance().runAsyncTimerTask(() -> {
			Homestead.database.exportLevels();
		}, 10, interval);
	}

	public List<Level> getAll() {
		return new ArrayList<>(this.values());
	}

	public void putOrUpdate(Level level) {
		this.put(level.getUniqueId(), level);
	}

	public long getLatency() {
		long before = System.currentTimeMillis();

		this.getAll();

		long after = System.currentTimeMillis();

		return after - before;
	}
}