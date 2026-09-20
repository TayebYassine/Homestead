package me.tayebyassine.homestead.api.events.region;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Fired when a region's display name is changed. Contains the old and new display names.
 */
public class RegionDisplaynameUpdateEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final String oldDisplayname;
    private final String newDisplayname;

    public RegionDisplaynameUpdateEvent(@NotNull Region region, @Nullable String oldDisplayname, @Nullable String newDisplayname) {
        this.region = region;
        this.oldDisplayname = oldDisplayname;
        this.newDisplayname = newDisplayname;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @Nullable String getOldDisplayname() {
        return oldDisplayname;
    }

    public @Nullable String getNewDisplayname() {
        return newDisplayname;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
