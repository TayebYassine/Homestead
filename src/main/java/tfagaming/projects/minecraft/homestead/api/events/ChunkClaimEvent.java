package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.models.Region;


public class ChunkClaimEvent extends APIEvent {
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
}
