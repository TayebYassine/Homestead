package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.Chunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.structure.Region;

public class ChunkUnclaimEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Region region;
	private final Chunk chunk;

	public ChunkUnclaimEvent(Region region, Chunk chunk) {
		this.region = region;
		this.chunk = chunk;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public Chunk getChunk() {
		return chunk;
	}

	public Region getRegion() {
		return region;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}
