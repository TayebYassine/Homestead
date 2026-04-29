package tfagaming.projects.minecraft.homestead.models;

import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

import java.util.UUID;

public final class RegionLog {
	private boolean autoUpdate = true;

	private final long id;
	private long regionId;
	private String author;
	private String message;
	private long sentAt;
	private boolean read;

	public RegionLog(long regionId, String author, String message) {
		this(regionId, author, message, System.currentTimeMillis(), false);
	}

	public RegionLog(long regionId, String author, String message, long sentAt, boolean read) {
		this.id = Homestead.getSnowflake().nextId();
		this.regionId = regionId;
		this.author = author;
		this.message = message.length() > 256 ? message.substring(0, 256) : message;
		this.sentAt = sentAt;
		this.read = read;
	}

	public RegionLog(long id, long regionId, String author, String message, long sentAt, boolean read) {
		this.id = id;
		this.regionId = regionId;
		this.author = author;
		this.message = message.length() > 256 ? message.substring(0, 256) : message;
		this.sentAt = sentAt;
		this.read = read;
	}

	/**
	 * Toggle Auto-Update for caching. If {@code true}, any call for setters will automatically
	 * update the cache. Otherwise, only the instance of the class will be updated.<br>
	 * @param autoUpdate Auto-Update toggle
	 */
	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public long getUniqueId() {
		return id;
	}

	public long getRegionId() {
		return regionId;
	}

	/**
	 * Returns the region by directly fetching with region ID from cache.
	 * @return The region if found, {@code null} otherwise.
	 */
	public @Nullable Region getRegion() {
		return RegionManager.findRegion(regionId);
	}

	/**
	 * Returns the region name safely by directly fetching with region ID from cache.
	 * @return The region name if found, {@code "?"} otherwise.
	 */
	public String getRegionName() {
		Region region = getRegion();

		return region == null ? "?" : region.getName();
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
		update();
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
		update();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message.length() > 256 ? message.substring(0, 256) : message;
		update();
	}

	public long getSentAt() {
		return sentAt;
	}

	public void setSentAt(long sentAt) {
		this.sentAt = sentAt;
		update();
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
		update();
	}

	private void update() {
		if (!autoUpdate) return;

		Homestead.LOG_CACHE.putOrUpdate(this);
	}
}