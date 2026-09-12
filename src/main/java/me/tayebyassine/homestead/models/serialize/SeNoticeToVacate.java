package me.tayebyassine.homestead.models.serialize;

import org.jetbrains.annotations.NotNull;

/**
 * Serializable representation of a notice-to-vacate for rented regions.
 *
 * <p>Tracks when the notice was issued, how many days the renter has to
 * vacate, and whether the renter has acknowledged the notice.
 * </p>
 */
public final class SeNoticeToVacate {

    private long noticeAt;
    private int daysToVacate;
    private boolean acknowledged;

    /**
     * Create a new notice to vacate.
     *
     * @param noticeAt     the timestamp when the notice was issued
     * @param daysToVacate the number of days the renter has to vacate
     */
    public SeNoticeToVacate(long noticeAt, int daysToVacate) {
        this.noticeAt = noticeAt;
        this.daysToVacate = daysToVacate;
        this.acknowledged = false;
    }

    /**
     * Deserialize a notice from its semicolon-separated string form.
     *
     * @param serialized the string in the format
     *                   {@code noticeAt;daysToVacate;acknowledged}
     * @return the deserialized notice, or {@code null} on parse failure
     */
    public static SeNoticeToVacate deserialize(@NotNull String serialized) {
        String[] split = serialized.split(";");

        try {
            long noticeAt = Long.parseLong(split[0]);
            int daysToVacate = Integer.parseInt(split[1]);
            boolean acknowledged = split.length > 2 && Boolean.parseBoolean(split[2]);

            SeNoticeToVacate notice = new SeNoticeToVacate(noticeAt, daysToVacate);
            notice.acknowledged = acknowledged;
            return notice;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the timestamp when the notice was issued.
     *
     * @return the notice timestamp in milliseconds
     */
    public long getNoticeAt() {
        return noticeAt;
    }

    /**
     * Set the timestamp when the notice was issued.
     *
     * @param noticeAt the notice timestamp in milliseconds
     */
    public void setNoticeAt(long noticeAt) {
        this.noticeAt = noticeAt;
    }

    /**
     * Get the number of days the renter has to vacate.
     *
     * @return the vacate period in days
     */
    public int getDaysToVacate() {
        return daysToVacate;
    }

    /**
     * Set the number of days the renter has to vacate.
     *
     * @param daysToVacate the vacate period in days
     */
    public void setDaysToVacate(int daysToVacate) {
        this.daysToVacate = daysToVacate;
    }

    /**
     * Calculate the absolute timestamp by which the renter must vacate.
     *
     * @return the vacate deadline in milliseconds
     */
    public long getVacateAt() {
        return noticeAt + (daysToVacate * 24L * 60 * 60 * 1000);
    }

    /**
     * Whether the renter has acknowledged the notice.
     *
     * @return {@code true} if acknowledged
     */
    public boolean isAcknowledged() {
        return acknowledged;
    }

    /**
     * Set whether the renter has acknowledged the notice.
     *
     * @param acknowledged {@code true} to mark as acknowledged
     */
    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    /**
     * Serialise the notice to a semicolon-separated string.
     *
     * @return the encoded string in the format
     * {@code noticeAt;daysToVacate;acknowledged}
     */
    public String serialize() {
        return String.format("%s;%s;%s", noticeAt, daysToVacate, acknowledged);
    }

    @Override
    public String toString() {
        return serialize();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SeNoticeToVacate other)) return false;
        return this.noticeAt == other.noticeAt
                && this.daysToVacate == other.daysToVacate
                && this.acknowledged == other.acknowledged;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(noticeAt, daysToVacate, acknowledged);
    }
}
