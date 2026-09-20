package me.tayebyassine.homestead.api.events.region;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


/**
 * Fired when a region's name is changed. Contains the old and new names.
 */
public class RegionNameUpdateEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final String oldName;
    private final String newName;

    public RegionNameUpdateEvent(@NotNull Region region, @NotNull String oldName, @NotNull String newName) {
        this.region = region;
        this.oldName = oldName;
        this.newName = newName;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @NotNull String getOldName() {
        return oldName;
    }

    public @NotNull String getNewName() {
        return newName;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
