package me.tayebyassine.homestead.api.events.region;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Fired when a region's owner is changed. Contains the old and new owner players.
 */
public class RegionOwnerUpdateEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Region region;
    private final OfflinePlayer oldOwner;
    private final OfflinePlayer newOwner;

    public RegionOwnerUpdateEvent(@NotNull Region region, @Nullable OfflinePlayer oldOwner, @NotNull OfflinePlayer newOwner) {
        this.region = region;
        this.oldOwner = oldOwner;
        this.newOwner = newOwner;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @Nullable OfflinePlayer getOldOwner() {
        return oldOwner;
    }

    public @NotNull OfflinePlayer getNewOwner() {
        return newOwner;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
