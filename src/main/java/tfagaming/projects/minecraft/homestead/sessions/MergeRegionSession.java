package tfagaming.projects.minecraft.homestead.sessions;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.Region;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MergeRegionSession {
	private static final Map<Long, Long> REQUESTS = new ConcurrentHashMap<>();

	private MergeRegionSession() {
	}

	public static void newMergeRequest(Region region, Region regionToMerge) {
		Long from = region.getUniqueId();
		Long to = regionToMerge.getUniqueId();

		if (REQUESTS.containsValue(to) || REQUESTS.containsValue(from) || REQUESTS.containsKey(to) || REQUESTS.containsKey(from)) {
			return;
		}

		REQUESTS.put(from, to);
		startTimer(region);
	}

	public static boolean isFromHaveRequest(Region from) {
		return REQUESTS.containsKey(from.getUniqueId());
	}

	public static boolean isToHaveRequest(Region to) {
		return REQUESTS.containsValue(to.getUniqueId());
	}

	public static long getFrom(Region to) {
		for (Map.Entry<Long, Long> e : REQUESTS.entrySet()) {
			if (e.getValue() == to.getUniqueId()) {
				return e.getKey();
			}
		}
		return -1L;
	}

	public static long getTo(Region from) {
		return REQUESTS.get(from.getUniqueId());
	}

	private static void startTimer(Region fromRegion) {
		final long id = fromRegion.getUniqueId();

		Homestead.getInstance().runAsyncTaskLater(() -> {
			REQUESTS.remove(id);
		}, 60);
	}
}
