package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public class UnbanPlayerEvent extends APIEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private final Region region;
    private final OfflinePlayer target;

    public UnbanPlayerEvent(@NotNull Region region, @NotNull OfflinePlayer target) {
        this.region = region;
        this.target = target;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    public @NotNull OfflinePlayer getUnbannedPlayer() {
        return target;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
