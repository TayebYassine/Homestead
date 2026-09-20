package me.tayebyassine.homestead.api.events.rent;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeRent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a notice is sent or retracted on a rental.
 */
public class RentNoticeEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final SubArea subArea;
    private final SeRent rent;
    private final boolean retracted;

    public RentNoticeEvent(@NotNull Region region, @Nullable SubArea subArea, @NotNull SeRent rent, boolean retracted) {
        this.region = region;
        this.subArea = subArea;
        this.rent = rent;
        this.retracted = retracted;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @Nullable SubArea getSubArea() {
        return subArea;
    }

    public @NotNull SeRent getRent() {
        return rent;
    }

    public boolean isRetracted() {
        return retracted;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
