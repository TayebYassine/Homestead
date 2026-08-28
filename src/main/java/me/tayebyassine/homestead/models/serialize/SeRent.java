package me.tayebyassine.homestead.models.serialize;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class SeRent {
    private static final Homestead INSTANCE = Homestead.getInstance();
    private UUID renterId;
    private long duration;
    private long startedAt;
    private long untilAt;
    private SeNoticeToVacate noticeToVacate;
    private double securityDeposit;
    private double price;

    public SeRent() {
        this.renterId = null;
        this.duration = Resources.<RegionsFile>get(ResourceType.Regions).getDefaultRentDays() * 24L * 60 * 60 * 1000;
        this.startedAt = 0L;
        this.untilAt = -1L;
        this.noticeToVacate = null;
        this.securityDeposit = Resources.<RegionsFile>get(ResourceType.Regions).getDefaultSecurityDeposit();
        this.price = Resources.<RegionsFile>get(ResourceType.Regions).getDefaultRentPrice();
    }

    public SeRent(UUID renterId, long duration, long startedAt, long untilAt, double securityDeposit, double price) {
        this.renterId = renterId;
        this.duration = duration;
        this.startedAt = startedAt;
        this.untilAt = untilAt;
        this.noticeToVacate = null;
        this.securityDeposit = securityDeposit;
        this.price = price;
    }

    public SeRent(OfflinePlayer renter, long duration, long startedAt, long untilAt, double securityDeposit, double price) {
        this(renter.getUniqueId(), duration, startedAt, untilAt, securityDeposit, price);
    }

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

    public UUID getRenterId() {
        return renterId;
    }

    public void setRenterId(UUID renterId) {
        this.renterId = renterId;
    }

    /**
     * Clears renter-specific fields while preserving owner's configuration (price, duration, deposit).
     * Called when rent expires or is cancelled.
     */
    public void clearRenter() {
        this.renterId = null;
        this.startedAt = 0L;
        this.untilAt = -1L;
        this.noticeToVacate = null;
    }

    public boolean isRenterer(OfflinePlayer player) {
        return isRenterer(player.getUniqueId());
    }

    public boolean isRenterer(UUID renterId) {
        return this.renterId != null && this.renterId.equals(renterId);
    }

    public @Nullable OfflinePlayer getRenter() {
        if (INSTANCE == null || renterId == null) return null;

        return INSTANCE.getOfflinePlayerSync(renterId);
    }

    public String getRenterName() {
        OfflinePlayer player = getRenter();

        return player == null ? "?" : player.getName();
    }

    public boolean hasRenter() {
        return renterId != null;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isPermanent() {
        return duration == -1L;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getUntilAt() {
        return untilAt;
    }

    public void setUntilAt(long untilAt) {
        this.untilAt = untilAt;
    }

    public boolean isExpired() {
        if (untilAt == -1L) return false;
        return System.currentTimeMillis() >= untilAt;
    }

    public @Nullable SeNoticeToVacate getNoticeToVacate() {
        return noticeToVacate;
    }

    public void setNoticeToVacate(@Nullable SeNoticeToVacate noticeToVacate) {
        this.noticeToVacate = noticeToVacate;
    }

    public boolean hasNoticeToVacate() {
        return noticeToVacate != null;
    }

    public double getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

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