package me.tayebyassine.homestead.models;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.RegionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a log entry for a region.
 *
 * <p>Log entries track author, message, timestamp, and read status.
 * Messages are capped at 256 characters.</p>
 */
public final class RegionLog {

    private final long id;
    private long regionId;
    private String author;
    private String message;
    private long sentAt;
    private boolean read;

    /**
     * Create a new log entry with a generated snowflake ID.
     *
     * @param regionId the region ID
     * @param author   the author name
     * @param message  the log message (truncated to 256 chars)
     */
    public RegionLog(long regionId, String author, String message) {
        this(regionId, author, message, System.currentTimeMillis(), false);
    }

    /**
     * Create a log entry with a generated snowflake ID.
     *
     * @param regionId the region ID
     * @param author   the author name
     * @param message  the log message
     * @param sentAt   the send timestamp
     * @param read     whether the entry has been read
     */
    public RegionLog(long regionId, String author, String message, long sentAt, boolean read) {
        this.id = Homestead.getSnowflake().nextId();
        this.regionId = regionId;
        this.author = author;
        this.message = message.length() > 256 ? message.substring(0, 256) : message;
        this.sentAt = sentAt;
        this.read = read;
    }

    /**
     * Create a log entry from pre-existing data (deserialization).
     *
     * @param id       the snowflake ID
     * @param regionId the region ID
     * @param author   the author name
     * @param message  the log message
     * @param sentAt   the send timestamp
     * @param read     whether the entry has been read
     */
    public RegionLog(long id, long regionId, String author, String message, long sentAt, boolean read) {
        this.id = id;
        this.regionId = regionId;
        this.author = author;
        this.message = message.length() > 256 ? message.substring(0, 256) : message;
        this.sentAt = sentAt;
        this.read = read;
    }

    /**
     * Get the unique snowflake ID.
     *
     * @return the log ID
     */
    public long getUniqueId() {
        return id;
    }

    /**
     * Get the region ID.
     *
     * @return the region ID
     */
    public long getRegionId() {
        return regionId;
    }

    /**
     * Set the region ID.
     *
     * @param regionId the new region ID
     */
    public void setRegionId(long regionId) {
        this.regionId = regionId;
        update();
    }

    /**
     * Get the parent region from the cache.
     *
     * @return the region, or {@code null} if not found
     */
    public @Nullable Region getRegion() {
        return RegionManager.findRegion(regionId);
    }

    /**
     * Get the parent region's name safely. Returns {@code "?"} if not
     * found.
     *
     * @return the region name
     */
    public @NotNull String getRegionName() {
        Region region = getRegion();

        return region == null ? "?" : region.getName();
    }

    /**
     * Get the author name.
     *
     * @return the author
     */
    public @NotNull String getAuthor() {
        return author;
    }

    /**
     * Set the author name.
     *
     * @param author the new author
     */
    public void setAuthor(@NotNull String author) {
        this.author = author;
        update();
    }

    /**
     * Get the log message.
     *
     * @return the message
     */
    public @NotNull String getMessage() {
        return message;
    }

    /**
     * Set the log message. Truncated to 256 characters.
     *
     * @param message the new message
     */
    public void setMessage(@NotNull String message) {
        this.message = message.length() > 256 ? message.substring(0, 256) : message;
        update();
    }

    /**
     * Get the send timestamp.
     *
     * @return the epoch-millis timestamp
     */
    public long getSentAt() {
        return sentAt;
    }

    /**
     * Set the send timestamp.
     *
     * @param sentAt the new timestamp
     */
    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
        update();
    }

    /**
     * Whether this entry has been read.
     *
     * @return {@code true} if read
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Set whether this entry has been read.
     *
     * @param read {@code true} to mark as read
     */
    public void setRead(boolean read) {
        this.read = read;
        update();
    }

    private void update() {
        Homestead.LOG_CACHE.putOrUpdate(this);
    }
}
