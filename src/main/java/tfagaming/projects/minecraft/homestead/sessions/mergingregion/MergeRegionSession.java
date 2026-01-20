package tfagaming.projects.minecraft.homestead.sessions.mergingregion;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.Region;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MergeRegionSession {
	private static final Map<UUID, UUID> requests = new ConcurrentHashMap<>();

	private MergeRegionSession() {
	}

	public static void newMergeRequest(Region region, Region regionToMerge) {
		UUID from = region.getUniqueId();
		UUID to = regionToMerge.getUniqueId();

		if (requests.containsValue(to) || requests.containsValue(from) || requests.containsKey(to) || requests.containsKey(from)) {
			return;
		}

		requests.put(from, to);
		startTimer(region);
	}

	public static boolean isFromHaveRequest(Region from) {
		return requests.containsKey(from.getUniqueId());
	}

	public static boolean isToHaveRequest(Region to) {
		return requests.containsValue(to.getUniqueId());
	}

	public static UUID getFrom(Region to) {
		for (Map.Entry<UUID, UUID> e : requests.entrySet()) {
			if (e.getValue().equals(to.getUniqueId())) {
				return e.getKey();
			}
		}
		return null;
	}

	public static UUID getTo(Region from) {
		return requests.get(from.getUniqueId());
	}

	private static void startTimer(Region fromRegion) {
		final UUID id = fromRegion.getUniqueId();

		Homestead.getInstance().runAsyncTaskLater(() -> {
			requests.remove(id);
		}, 60);
	}
}
