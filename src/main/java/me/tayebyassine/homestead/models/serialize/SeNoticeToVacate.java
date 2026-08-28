package me.tayebyassine.homestead.models.serialize;

import org.jetbrains.annotations.NotNull;

public final class SeNoticeToVacate {
    private long noticeAt;
    private int daysToVacate;
    private boolean acknowledged;

    public SeNoticeToVacate(long noticeAt, int daysToVacate) {
        this.noticeAt = noticeAt;
        this.daysToVacate = daysToVacate;
        this.acknowledged = false;
    }

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

    public long getNoticeAt() {
        return noticeAt;
    }

    public void setNoticeAt(long noticeAt) {
        this.noticeAt = noticeAt;
    }

    public int getDaysToVacate() {
        return daysToVacate;
    }

    public void setDaysToVacate(int daysToVacate) {
        this.daysToVacate = daysToVacate;
    }

    public long getVacateAt() {
        return noticeAt + (daysToVacate * 24L * 60 * 60 * 1000);
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

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