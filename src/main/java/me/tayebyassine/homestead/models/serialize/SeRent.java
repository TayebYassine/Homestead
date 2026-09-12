package me.tayebyassine.homestead.models.serialize;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Serializable representation of rental data for a region or sub-area.
 *
 * <p>Stores the renter, duration, timestamps, security deposit, price,
 * and optional notice-to-vacate. Can be serialized to and deserialized
 * from a comma-separated string for persistence.
 * </p>
 */
public final class SeRent {

    private static final Homestead INSTANCE = Homestead.getInstance();
    private UUID renterId;
    private long duration;
    private long startedAt;
    private long untilAt;
    private SeNoticeToVacate noticeToVacate;
    private double securityDeposit;
    private double price;

    /**
     * Create a default rent with values from the regions configuration.
     */
    public SeRent() {
        this.renterId = null;
        this.duration = Resources.<RegionsFile>get(ResourceType.Regions).getDefaultRentDays() * 24L * 60 * 60 * 1000;
        this.startedAt = 0L;
        this.untilAt = -1L;
        this.noticeToVacate = null;
        this.securityDeposit = Resources.<RegionsFile>get(ResourceType.Regions).getDefaultSecurityDeposit();
        this.price = Resources.<RegionsFile>get(ResourceType.Regions).getDefaultRentPrice();
    }

    /**
     * Create a rent with the given parameters.
     *
     * @param renterId        the renter's UUID, or {@code null} for no renter
     * @param duration        the rental duration in milliseconds
     * @param startedAt       the timestamp when the rental started
     * @param untilAt         the timestamp when the rental ends, or
     *                        {@code -1} for permanent
     * @param securityDeposit the security deposit amount
     * @param price           the rental price
     */
    public SeRent(UUID renterId, long duration, long startedAt, long untilAt, double securityDeposit, double price) {
        this.renterId = renterId;
        this.duration = duration;
        this.startedAt = startedAt;
        this.untilAt = untilAt;
        this.noticeToVacate = null;
        this.securityDeposit = securityDeposit;
        this.price = price;
    }

    /**
     * Create a rent with the given parameters from an offline player.
     *
     * @param renter          the renter
     * @param duration        the rental duration in milliseconds
     * @param startedAt       the timestamp when the rental started
     * @param untilAt         the timestamp when the rental ends
     * @param securityDeposit the security deposit amount
     * @param price           the rental price
     */
    public SeRent(OfflinePlayer renter, long duration, long startedAt, long untilAt, double securityDeposit, double price) {
        this(renter.getUniqueId(), duration, startedAt, untilAt, securityDeposit, price);
    }

    /**
     * Deserialize a rent from its comma-separated string form.
     *
     * @param serialized the encoded rent string
     * @return the deserialized rent, or a default rent on parse failure
     */
    public static SeRent deserialize(@NotNull String serialized) {
        String[] split = serialized.split(",");

        try {
            UUID playerId = null;
            if (!split[0].equals("null")) {
                playerId = UUID.fromString(split[0]);

                if (INSTANCE.getOfflinePlayerSync(playerId) == null) return new SeRent();
            }

            long duration = Long.parseLong(split[1]);
            long startedAt = Long.parseLong(split[2]);
            long untilAt = Long.parseLong(split[3]);
            double securityDeposit = Double.parseDouble(split[4]);
            double price = Double.parseDouble(split[5]);

            SeRent rent = new SeRent(playerId, duration, startedAt, untilAt, securityDeposit, price);

            if (split.length > 6 && !split[6].equals("null")) {
                rent.setNoticeToVacate(SeNoticeToVacate.deserialize(split[6]));
            }

            return rent;
        } catch (Exception e) {
            return new SeRent();
        }
    }

    /**
     * Get the renter's UUID.
     *
     * @return the renter UUID, or {@code null} if unoccupied
     */
    public UUID getRenterId() {
        return renterId;
    }

    /**
     * Set the renter's UUID.
     *
     * @param renterId the new renter UUID
     */
    public void setRenterId(UUID renterId) {
        this.renterId = renterId;
    }

    /**
     * Clear renter-specific fields while preserving owner configuration
     * (price, duration, deposit). Called when rent expires or is cancelled.
     */
    public void clearRenter() {
        this.renterId = null;
        this.startedAt = 0L;
        this.untilAt = -1L;
        this.noticeToVacate = null;
    }

    /**
     * Check whether the given player is the current renter.
     *
     * @param player the player to check
     * @return {@code true} if the player is the renter
     */
    public boolean isRenterer(OfflinePlayer player) {
        return isRenterer(player.getUniqueId());
    }

    /**
     * Check whether the given UUID is the current renter.
     *
     * @param renterId the UUID to check
     * @return {@code true} if the UUID matches the renter
     */
    public boolean isRenterer(UUID renterId) {
        return this.renterId != null && this.renterId.equals(renterId);
    }

    /**
     * Get the renter as an offline player.
     *
     * @return the renter, or {@code null} if no renter or not found
     */
    public @Nullable OfflinePlayer getRenter() {
        if (INSTANCE == null || renterId == null) return null;

        return INSTANCE.getOfflinePlayerSync(renterId);
    }

    /**
     * Get the renter's display name safely.
     *
     * @return the renter's name, or {@code "?"} if not found
     */
    public String getRenterName() {
        OfflinePlayer player = getRenter();

        return player == null ? "?" : player.getName();
    }

    /**
     * Whether this rent currently has a renter.
     *
     * @return {@code true} if a renter is assigned
     */
    public boolean hasRenter() {
        return renterId != null;
    }

    /**
     * Get the rental duration in milliseconds.
     *
     * @return the duration, or {@code -1} for permanent
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Set the rental duration in milliseconds.
     *
     * @param duration the new duration
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Whether this is a permanent (non-expiring) rental.
     *
     * @return {@code true} if the duration is {@code -1}
     */
    public boolean isPermanent() {
        return duration == -1L;
    }

    /**
     * Get the timestamp when the rental started.
     *
     * @return the start timestamp in milliseconds
     */
    public long getStartedAt() {
        return startedAt;
    }

    /**
     * Set the timestamp when the rental started.
     *
     * @param startedAt the start timestamp in milliseconds
     */
    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * Get the timestamp when the rental ends.
     *
     * @return the end timestamp, or {@code -1} for permanent
     */
    public long getUntilAt() {
        return untilAt;
    }

    /**
     * Set the timestamp when the rental ends.
     *
     * @param untilAt the end timestamp, or {@code -1} for permanent
     */
    public void setUntilAt(long untilAt) {
        this.untilAt = untilAt;
    }

    /**
     * Whether the rental has expired.
     *
     * @return {@code true} if the current time is past the end timestamp
     */
    public boolean isExpired() {
        if (untilAt == -1L) return false;
        return System.currentTimeMillis() >= untilAt;
    }

    /**
     * Get the notice-to-vacate, if any.
     *
     * @return the notice, or {@code null}
     */
    public @Nullable SeNoticeToVacate getNoticeToVacate() {
        return noticeToVacate;
    }

    /**
     * Set the notice-to-vacate.
     *
     * @param noticeToVacate the notice, or {@code null} to clear
     */
    public void setNoticeToVacate(@Nullable SeNoticeToVacate noticeToVacate) {
        this.noticeToVacate = noticeToVacate;
    }

    /**
     * Whether a notice-to-vacate has been issued.
     *
     * @return {@code true} if a notice is active
     */
    public boolean hasNoticeToVacate() {
        return noticeToVacate != null;
    }

    /**
     * Get the security deposit amount.
     *
     * @return the deposit amount
     */
    public double getSecurityDeposit() {
        return securityDeposit;
    }

    /**
     * Set the security deposit amount.
     *
     * @param securityDeposit the new deposit amount
     */
    public void setSecurityDeposit(double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    /**
     * Get the rental price.
     *
     * @return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Set the rental price.
     *
     * @param price the new price
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Serialize the rent to a comma-separated string.
     *
     * @return the encoded string
     */
    public String serialize() {
        String noticeStr = noticeToVacate != null ? noticeToVacate.serialize() : "null";
        return String.format("%s,%s,%s,%s,%s,%s,%s",
                renterId != null ? renterId.toString() : "null",
                duration,
                startedAt,
                untilAt,
                securityDeposit,
                price,
                noticeStr
        );
    }

    @Override
    public String toString() {
        return serialize();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SeRent other)) return false;
        return java.util.Objects.equals(this.renterId, other.renterId)
                && this.duration == other.duration
                && this.startedAt == other.startedAt
                && this.untilAt == other.untilAt
                && this.securityDeposit == other.securityDeposit
                && this.price == other.price
                && java.util.Objects.equals(this.noticeToVacate, other.noticeToVacate);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(renterId, duration, startedAt, untilAt, securityDeposit, price, noticeToVacate);
    }
}
