package me.tayebyassine.homestead.api.events.chunk;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.models.Region;
import org.bukkit.Chunk;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


/**
 * Fired when chunks are about to be claimed. Cancel to prevent claiming.
 */
public class ChunkClaimEvent extends APIEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private final Region region;
    private final Chunk chunk;

    public ChunkClaimEvent(@NotNull Region region, @NotNull Chunk chunk) {
        this.region = region;
        this.chunk = chunk;
    }

    public @NotNull Chunk getChunk() {
        return chunk;
    }

    public @NotNull Region getRegion() {
        return region;
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
