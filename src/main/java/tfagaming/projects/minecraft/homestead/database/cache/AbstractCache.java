package tfagaming.projects.minecraft.homestead.database.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCache<V> extends ConcurrentHashMap<UUID, V> implements Cache<UUID, V> {
	protected abstract UUID getId(V item);

	@Override
	public List<V> getAll() {
		return new ArrayList<>(this.values());
	}

	@Override
	public void putAll(List<V> items) {
		for (V item : items) {
			putOrUpdate(item);
		}
	}

	@Override
	public void putOrUpdate(V item) {
		this.put(getId(item), item);
	}

	@Override
	public long getLatency() {
		long before = System.currentTimeMillis();
		this.getAll();
		return System.currentTimeMillis() - before;
	}
}
