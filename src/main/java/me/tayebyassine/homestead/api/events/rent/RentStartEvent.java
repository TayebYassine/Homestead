package me.tayebyassine.homestead.api.events.rent;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeRent;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a rent is started and money is exchanged.
 * Cancel to prevent the rental from being created.
 */
public class RentStartEvent extends APIEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private final Region region;
    private final SubArea subArea;
    private final OfflinePlayer renter;
    private final SeRent rent;

    public RentStartEvent(@NotNull Region region, @Nullable SubArea subArea, @NotNull OfflinePlayer renter, @NotNull SeRent rent) {
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
