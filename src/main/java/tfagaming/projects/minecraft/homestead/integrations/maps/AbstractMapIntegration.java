package tfagaming.projects.minecraft.homestead.integrations.maps;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionChunk;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;

import java.util.List;
import java.util.UUID;

public abstract class AbstractMapIntegration {

	protected static final String LAYER_LABEL = "Homestead Regions";
	protected static final String CONFIG_PATH = "dynamic-maps";

	protected final Homestead plugin;

	protected AbstractMapIntegration(Homestead plugin) {
		this.plugin = plugin;
	}

	public abstract void clearAllMarkers();

	public abstract void update();

	protected World resolveRegionWorld(Region region) {
		if (region.getLocation() != null && region.getLocation().getWorld() != null) {
			return region.getLocation().getWorld();
		}

		for (RegionChunk chunk : ChunkManager.getChunksOfRegion(region)) {
			if (chunk == null) continue;

			World world = chunk.getWorld();
			if (world != null) return world;

			if (chunk.getWorldId() != null) {
				World byName = org.bukkit.Bukkit.getWorld(chunk.getWorldId());
				if (byName != null) return byName;
			}
		}
		return null;
	}

	protected String resolveHoverText(Region region, boolean isOperator) {
		List<RegionChunk> chunks = ChunkManager.getChunksOfRegion(region);

		OfflinePlayer owner = region.getOwner();

		Placeholder placeholder = new Placeholder()
				.add("{region}", region.getName())
				.add("{region-owner}", owner == null ? "UNKNOWN PLAYER" : owner.getName())
				.add("{region-members}", ColorTranslator.preserve(Formatter.getMembersOfRegion(region)))
				.add("{region-chunks}", chunks.size())
				.add("{global-rank}", RegionManager.getGlobalRank(region.getUniqueId()))
				.add("{region-description}", region.getDescription())
				.add("{region-size}", chunks.size() * 256);

		String path = isOperator ? "chunks.operator-description" : "chunks.description";
		return Formatter.applyPlaceholders(getConfigString(path), placeholder);
	}

	protected String resolvePlainLabel(Region region) {
		String label = region.getName() + " (#" + RegionManager.getGlobalRank(region.getUniqueId()) + ")";
		return ColorTranslator.preserve(label)
				.replaceAll("<[^>]*>", "")
				.replaceAll("&lt;[^&]*&gt;", "")
				.trim();
	}

	protected int resolveChunkColor(Region region, boolean isOperator) {
		if (region.getMapColor() != 0) {
			return region.getMapColor();
		}
		String path = isOperator ? "chunks.operator-color" : "chunks.color";
		return getConfigInt(path);
	}

	protected boolean isNeighborClaimed(Region region, RegionChunk chunk, GeoDirection direction) {
		int x = chunk.getX();
		int z = chunk.getZ();
		UUID worldId = chunk.getWorldId();

		switch (direction) {
			case NORTH -> z -= 1;
			case EAST  -> x += 1;
			case SOUTH -> z += 1;
			case WEST  -> x -= 1;
			default -> { return false; }
		}

		final int nx = x, nz = z;
		return ChunkManager.getChunksOfRegion(region).stream()
				.anyMatch(c -> c.getWorldId().equals(worldId) && c.getX() == nx && c.getZ() == nz);
	}

	protected String getConfigString(String path) {
		return Resources.<ConfigFile>get(ResourceType.Config).getString(CONFIG_PATH + "." + path);
	}

	protected int getConfigInt(String path) {
		return Resources.<ConfigFile>get(ResourceType.Config).getInt(CONFIG_PATH + "." + path);
	}

	protected boolean getConfigBoolean(String path) {
		return Resources.<ConfigFile>get(ResourceType.Config).getBoolean(CONFIG_PATH + "." + path);
	}

	protected int getTransparencyFill() {
		return getConfigInt("chunks.transparency-fill");
	}

	protected int getTransparencyOutline() {
		return getConfigInt("chunks.transparency-outline");
	}

	protected enum GeoDirection {
		NORTH, EAST, SOUTH, WEST
	}
}