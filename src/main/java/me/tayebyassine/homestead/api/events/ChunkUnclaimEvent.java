package me.tayebyassine.homestead.api.events;

import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;
import me.tayebyassine.homestead.models.Region;


public class ChunkUnclaimEvent extends APIEvent {
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
}
