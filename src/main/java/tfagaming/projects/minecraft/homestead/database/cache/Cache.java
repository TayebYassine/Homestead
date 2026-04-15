package tfagaming.projects.minecraft.homestead.database.cache;

import java.util.List;

public interface Cache<K, V> {
	List<V> getAll();

	void putAll(List<V> items);

	void putOrUpdate(V item);

	long getLatency();
}
