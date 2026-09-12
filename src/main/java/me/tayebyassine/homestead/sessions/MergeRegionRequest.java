package me.tayebyassine.homestead.sessions;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.models.Region;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks pending merge requests between regions.
 *
 * <p>A merge request is created when a region owner initiates a merge with
 * another region. The request expires after 60 seconds if not accepted.
 * </p>
 */
public final class MergeRegionRequest {

    /**
     * Maps source region ID to target region ID.
     */
    public static final Map<Long, Long> REQUESTS = new ConcurrentHashMap<>();

    private MergeRegionRequest() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Create a new merge request from one region to another.
     *
     * <p>If either region already has a pending request (as source or
     * target), the new request is silently ignored.
     * </p>
     *
     * @param from the region to merge from (will be deleted on accept)
     * @param to   the region to merge into
     */
    public static void newMergeRequest(Region from, Region to) {
        long fromId = from.getUniqueId();
        long toId = to.getUniqueId();

        if (REQUESTS.containsValue(toId) || REQUESTS.containsValue(fromId) || REQUESTS.containsKey(toId) || REQUESTS.containsKey(fromId)) {
            return;
        }

        REQUESTS.put(fromId, toId);
        startTimer(from);
    }

    /**
     * Check whether the given region has a pending merge request as the
     * source.
     *
     * @param from the region to check
     * @return {@code true} if the region has an outgoing merge request
     */
    public static boolean isFromHaveRequest(Region from) {
        return REQUESTS.containsKey(from.getUniqueId());
    }

    /**
     * Check whether the given region has a pending merge request as the
     * target.
     *
     * @param to the region to check
     * @return {@code true} if the region is the target of a merge request
     */
    public static boolean isToHaveRequest(Region to) {
        return REQUESTS.containsValue(to.getUniqueId());
    }

    /**
     * Get the source region ID for a merge request targeting the given
     * region.
     *
     * @param to the target region
     * @return the source region ID, or {@code -1} if no request exists
     */
    public static long getFrom(Region to) {
        for (Map.Entry<Long, Long> e : REQUESTS.entrySet()) {
            if (e.getValue() == to.getUniqueId()) {
                return e.getKey();
            }
        }
        return -1L;
    }

    /**
     * Get the target region ID for a merge request originating from the
     * given region.
     *
     * @param from the source region
     * @return the target region ID
     */
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
