package me.tayebyassine.homestead.api.events.region;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Fired when a region's spawn location is changed. Contains the old and new locations.
 */
public class RegionLocationUpdateEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final Location oldLocation;
    private final Location newLocation;

    public RegionLocationUpdateEvent(@NotNull Region region, @Nullable Location oldLocation, @Nullable Location newLocation) {
        this.region = region;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @Nullable Location getOldLocation() {
        return oldLocation;
    }

    public @Nullable Location getNewLocation() {
        return newLocation;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
