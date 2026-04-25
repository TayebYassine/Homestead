package tfagaming.projects.minecraft.homestead.managers;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionLog;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link RegionLog}.
 */
public final class LogManager {
	private LogManager() {}

	/**
	 * Add a new log to a region.
	 * @param region The region
	 * @param author The author of the message
	 * @param message The message
	 */
	public static void addLog(Region region, String author, String message) {
		addLog(region.getUniqueId(), author, message);
	}

	/**
	 * Add a new log to a region.
	 * @param regionId The region ID
	 * @param author The author of the message
	 * @param message The message
	 */
	public static void addLog(long regionId, String author, String message) {
		RegionLog log = new RegionLog(regionId, author, message);

		Homestead.regionLogCache.putOrUpdate(log);
	}

	/**
	 * Returns a list of logs from a region.
	 * @param region The region
	 * @return List of logs from a region
	 */
	public static List<RegionLog> getLogs(Region region) {
		return getLogs(region.getUniqueId());
	}

	/**
	 * Returns a list of logs from a region.
	 * @param regionId The region ID
	 * @return List of logs from a region
	 */
	public static List<RegionLog> getLogs(long regionId) {
		return Homestead.regionLogCache.getAll().stream()
				.filter(l -> l.getRegionId() == regionId)
				.sorted(Comparator.comparingLong(RegionLog::getSentAt).reversed())
				.collect(Collectors.toList());
	}

	/**
	 * Delete a log.
	 * @param log The log
	 */
	public static void deleteLog(RegionLog log) {
		deleteLog(log.getUniqueId());
	}

	/**
	 * Delete a log.
	 * @param logId The log ID
	 */
	public static void deleteLog(long logId) {
		Homestead.regionLogCache.remove(logId);
	}

	/**
	 * Delete all logs from a region.
	 * @param region The region
	 */
	public static void deleteLogsOfRegion(Region region) {
		deleteLogsOfRegion(region.getUniqueId());
	}

	/**
	 * Delete all logs from a region.
	 * @param regionId The region ID
	 */
	public static void deleteLogsOfRegion(long regionId) {
		for (RegionLog log : getLogs(regionId)) {
			deleteLog(log.getUniqueId());
		}
	}

	/**
	 * Mark all logs as read.
	 * @param region The region
	 */
	public static void markAllAsRead(Region region) {
		markAllAsRead(region.getUniqueId());
	}

	/**
	 * Mark all logs as read.
	 * @param regionId The region ID
	 */
	public static void markAllAsRead(long regionId) {
		getLogs(regionId).forEach(l -> {
			l.setRead(true);
		});
	}
}