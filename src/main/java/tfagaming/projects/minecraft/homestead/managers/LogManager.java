package tfagaming.projects.minecraft.homestead.managers;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionLog;

import java.util.ArrayList;
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
	 * Retrieves a specific log by its unique ID.
	 * @param logId The log ID
	 * @return The {@link RegionLog}, or {@code null} if not found.
	 */
	public static RegionLog getLog(long logId) {
		return Homestead.regionLogCache.get(logId);
	}

	/**
	 * Returns a list of logs from a region, sorted newest first.
	 * @param region The region
	 * @return List of logs from a region
	 */
	public static List<RegionLog> getLogs(Region region) {
		return getLogs(region.getUniqueId());
	}

	/**
	 * Returns a list of logs from a region, sorted newest first.
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
	 * Returns only unread logs from a region.
	 * @param region The region
	 * @return List of unread logs.
	 */
	public static List<RegionLog> getUnreadLogs(Region region) {
		return getUnreadLogs(region.getUniqueId());
	}

	/**
	 * Returns only unread logs from a region.
	 * @param regionId The region ID
	 * @return List of unread logs.
	 */
	public static List<RegionLog> getUnreadLogs(long regionId) {
		return getLogs(regionId).stream()
				.filter(l -> !l.isRead())
				.collect(Collectors.toList());
	}

	/**
	 * Returns the number of unread logs in a region.
	 * @param region The region
	 * @return Unread log count.
	 */
	public static int getUnreadCount(Region region) {
		return getUnreadCount(region.getUniqueId());
	}

	/**
	 * Returns the number of unread logs in a region.
	 * @param regionId The region ID
	 * @return Unread log count.
	 */
	public static int getUnreadCount(long regionId) {
		return (int) getLogs(regionId).stream()
				.filter(l -> !l.isRead())
				.count();
	}

	/**
	 * Returns the total number of logs in a region.
	 * @param region The region
	 * @return Total log count.
	 */
	public static int getLogCount(Region region) {
		return getLogCount(region.getUniqueId());
	}

	/**
	 * Returns the total number of logs in a region.
	 * @param regionId The region ID
	 * @return Total log count.
	 */
	public static int getLogCount(long regionId) {
		return (int) Homestead.regionLogCache.getAll().stream()
				.filter(l -> l.getRegionId() == regionId)
				.count();
	}

	/**
	 * Checks if a region has any unread logs.
	 * @param region The region
	 * @return {@code true} if unread logs exist.
	 */
	public static boolean hasUnreadLogs(Region region) {
		return hasUnreadLogs(region.getUniqueId());
	}

	/**
	 * Checks if a region has any unread logs.
	 * @param regionId The region ID
	 * @return {@code true} if unread logs exist.
	 */
	public static boolean hasUnreadLogs(long regionId) {
		return getUnreadCount(regionId) > 0;
	}

	/**
	 * Returns logs from a specific author in a region.
	 * @param region The region
	 * @param author The author name
	 * @return List of logs by the author.
	 */
	public static List<RegionLog> getLogsByAuthor(Region region, String author) {
		return getLogsByAuthor(region.getUniqueId(), author);
	}

	/**
	 * Returns logs from a specific author in a region.
	 * @param regionId The region ID
	 * @param author The author name
	 * @return List of logs by the author.
	 */
	public static List<RegionLog> getLogsByAuthor(long regionId, String author) {
		return getLogs(regionId).stream()
				.filter(l -> l.getAuthor().equalsIgnoreCase(author))
				.collect(Collectors.toList());
	}

	/**
	 * Returns logs sent before a specific timestamp.
	 * @param region The region
	 * @param timestamp The cutoff timestamp (exclusive)
	 * @return List of older logs.
	 */
	public static List<RegionLog> getLogsBefore(Region region, long timestamp) {
		return getLogsBefore(region.getUniqueId(), timestamp);
	}

	/**
	 * Returns logs sent before a specific timestamp.
	 * @param regionId The region ID
	 * @param timestamp The cutoff timestamp (exclusive)
	 * @return List of older logs.
	 */
	public static List<RegionLog> getLogsBefore(long regionId, long timestamp) {
		return getLogs(regionId).stream()
				.filter(l -> l.getSentAt() < timestamp)
				.collect(Collectors.toList());
	}

	/**
	 * Returns logs sent after a specific timestamp.
	 * @param region The region
	 * @param timestamp The cutoff timestamp (exclusive)
	 * @return List of newer logs.
	 */
	public static List<RegionLog> getLogsAfter(Region region, long timestamp) {
		return getLogsAfter(region.getUniqueId(), timestamp);
	}

	/**
	 * Returns logs sent after a specific timestamp.
	 * @param regionId The region ID
	 * @param timestamp The cutoff timestamp (exclusive)
	 * @return List of newer logs.
	 */
	public static List<RegionLog> getLogsAfter(long regionId, long timestamp) {
		return getLogs(regionId).stream()
				.filter(l -> l.getSentAt() > timestamp)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the oldest log in a region.
	 * @param region The region
	 * @return The oldest log, or {@code null} if none exist.
	 */
	public static RegionLog getOldestLog(Region region) {
		return getOldestLog(region.getUniqueId());
	}

	/**
	 * Returns the oldest log in a region.
	 * @param regionId The region ID
	 * @return The oldest log, or {@code null} if none exist.
	 */
	public static RegionLog getOldestLog(long regionId) {
		return getLogs(regionId).stream()
				.min(Comparator.comparingLong(RegionLog::getSentAt))
				.orElse(null);
	}

	/**
	 * Returns the most recent log in a region.
	 * @param region The region
	 * @return The latest log, or {@code null} if none exist.
	 */
	public static RegionLog getLatestLog(Region region) {
		return getLatestLog(region.getUniqueId());
	}

	/**
	 * Returns the most recent log in a region.
	 * @param regionId The region ID
	 * @return The latest log, or {@code null} if none exist.
	 */
	public static RegionLog getLatestLog(long regionId) {
		return getLogs(regionId).stream()
				.findFirst()
				.orElse(null);
	}

	/**
	 * Marks a specific log as read.
	 * @param logId The log ID
	 * @return {@code true} if the log was found and updated.
	 */
	public static boolean markAsRead(long logId) {
		RegionLog log = getLog(logId);
		if (log == null) return false;
		log.setRead(true);
		return true;
	}

	/**
	 * Marks a specific log as unread.
	 * @param logId The log ID
	 * @return {@code true} if the log was found and updated.
	 */
	public static boolean markAsUnread(long logId) {
		RegionLog log = getLog(logId);
		if (log == null) return false;
		log.setRead(false);
		return true;
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
		getLogs(regionId).forEach(l -> l.setRead(true));
	}

	/**
	 * Mark all logs as unread.
	 * @param region The region
	 */
	public static void markAllAsUnread(Region region) {
		markAllAsUnread(region.getUniqueId());
	}

	/**
	 * Mark all logs as unread.
	 * @param regionId The region ID
	 */
	public static void markAllAsUnread(long regionId) {
		getLogs(regionId).forEach(l -> l.setRead(false));
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
	 * Deletes all read logs from a region.
	 * @param region The region
	 * @return The number of logs deleted.
	 */
	public static int deleteReadLogs(Region region) {
		return deleteReadLogs(region.getUniqueId());
	}

	/**
	 * Deletes all read logs from a region.
	 * @param regionId The region ID
	 * @return The number of logs deleted.
	 */
	public static int deleteReadLogs(long regionId) {
		List<Long> toRemove = getLogs(regionId).stream()
				.filter(RegionLog::isRead)
				.map(RegionLog::getUniqueId)
				.toList();

		for (Long id : toRemove) {
			deleteLog(id);
		}
		return toRemove.size();
	}

	/**
	 * Deletes all logs older than the specified timestamp.
	 * @param timestamp The cutoff timestamp (exclusive)
	 * @return The number of logs deleted.
	 */
	public static int deleteLogsOlderThan(long timestamp) {
		List<Long> toRemove = Homestead.regionLogCache.getAll().stream()
				.filter(l -> l.getSentAt() < timestamp)
				.map(RegionLog::getUniqueId)
				.toList();

		for (Long id : toRemove) {
			deleteLog(id);
		}
		return toRemove.size();
	}

	/**
	 * Deletes every log in the cache. Use with caution.
	 * @return The number of logs deleted.
	 */
	public static int deleteAllLogs() {
		List<Long> ids = Homestead.regionLogCache.getAll().stream()
				.map(RegionLog::getUniqueId)
				.toList();

		for (Long id : ids) {
			deleteLog(id);
		}
		return ids.size();
	}

	/**
	 * Removes all logs with invalid references:<br>
	 * - Regions that no longer exist
	 * @return Number of corrupted logs removed.
	 */
	public static int cleanupInvalidLogs() {
		List<Long> toRemove = new ArrayList<>();

		for (RegionLog log : Homestead.regionLogCache.getAll()) {
			boolean invalidRegion = log.getRegion() == null;

			if (invalidRegion) {
				toRemove.add(log.getUniqueId());
			}
		}

		for (Long id : toRemove) {
			deleteLog(id);
		}
		return toRemove.size();
	}
}