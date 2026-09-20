package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


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
