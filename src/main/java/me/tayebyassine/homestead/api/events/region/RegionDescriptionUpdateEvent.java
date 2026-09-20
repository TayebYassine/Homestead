package me.tayebyassine.homestead.api.events.region;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Fired when a region's description is changed. Contains the old and new descriptions.
 */
public class RegionDescriptionUpdateEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final String oldDescription;
    private final String newDescription;

    public RegionDescriptionUpdateEvent(@NotNull Region region, @Nullable String oldDescription, @Nullable String newDescription) {
        this.region = region;
        this.oldDescription = oldDescription;
        this.newDescription = newDescription;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @Nullable String getOldDescription() {
        return oldDescription;
    }

    public @Nullable String getNewDescription() {
        return newDescription;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
