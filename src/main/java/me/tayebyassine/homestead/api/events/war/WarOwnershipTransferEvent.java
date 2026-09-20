package me.tayebyassine.homestead.api.events.war;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.War;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when ownership of a region is transferred as a result of an ownership war.
 */
public class WarOwnershipTransferEvent extends APIEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final War war;
    private final Region region;
    private final OfflinePlayer oldOwner;
    private final OfflinePlayer newOwner;

    public WarOwnershipTransferEvent(@NotNull War war, @NotNull Region region,
                                     @NotNull OfflinePlayer oldOwner, @NotNull OfflinePlayer newOwner) {
        this.war = war;
        this.region = region;
        this.oldOwner = oldOwner;
        this.newOwner = newOwner;
    }

    public @NotNull War getWar() {
        return war;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @NotNull OfflinePlayer getOldOwner() {
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
