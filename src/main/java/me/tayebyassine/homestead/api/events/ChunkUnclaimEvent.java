package me.tayebyassine.homestead.api.events;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.Chunk;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public class ChunkUnclaimEvent extends APIEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private final Region region;
    private final Chunk chunk;

    public ChunkUnclaimEvent(@NotNull Region region, @NotNull Chunk chunk) {
        this.region = region;
        this.chunk = chunk;
    }

    public @NotNull Chunk getChunk() {
        return chunk;
    }

    public @NotNull Region getRegion() {
        return region;
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
