package me.tayebyassine.homestead.api.events.rent;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeRent;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a rent expires and access is revoked.
 */
public class RentExpireEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final SubArea subArea;
    private final OfflinePlayer renter;
    private final SeRent rent;

    public RentExpireEvent(@NotNull Region region, @Nullable SubArea subArea, @NotNull OfflinePlayer renter, @NotNull SeRent rent) {
        this.region = region;
        this.subArea = subArea;
        this.renter = renter;
        this.rent = rent;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @Nullable SubArea getSubArea() {
        return subArea;
    }

    public @NotNull OfflinePlayer getRenter() {
        return renter;
    }

    public @NotNull SeRent getRent() {
        return rent;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
