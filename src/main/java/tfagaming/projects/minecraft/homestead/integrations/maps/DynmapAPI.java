package tfagaming.projects.minecraft.homestead.integrations.maps;

import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.integrations.maps.listeners.DynmapListener;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class DynmapAPI {
	public static MarkerSet markerSet;

	public DynmapAPI(Homestead plugin) {
		try {
			DynmapCommonAPIListener.register(new DynmapListener());
		} catch (NoClassDefFoundError e) {

		}
	}

	public void clearAllMarkers() {
		if (markerSet == null) {
			return;
		}

		for (AreaMarker marker : markerSet.getAreaMarkers()) {
			marker.deleteMarker();
		}
	}

	public void addChunkMarker(Region region, SerializableChunk chunk) {
		if (markerSet == null) {
			return;
		}

		double[] x = {chunk.getX() * 16, (chunk.getX() + 1) * 16};
		double[] z = {chunk.getZ() * 16, (chunk.getZ() + 1) * 16};

		String markerId = "claimed_" + chunk.getWorldName() + "_" + chunk.getX() + "_" + chunk.getZ();

		AreaMarker existingMarker = markerSet.findAreaMarker(markerId);

		if (existingMarker != null) {
			return;
		}

		String markerLabel = region.getName();

		AreaMarker areaMarker = markerSet.createAreaMarker(
				markerId,
				markerLabel,
				false,
				chunk.getWorldName(),
				x,
				z,
				false);

		if (areaMarker == null) {
			return;
		}

		int chunkTransparencyInfill = Homestead.config.getInt("dynamic-maps.chunks.transparency-fill");
		int chunkTransparencyOutline = Homestead.config.getInt("dynamic-maps.chunks.transparency-outline");

		boolean isOperator = PlayerUtils.isOperator(region.getOwner());

		int chunkColor = region.getMapColor() == 0
				? (isOperator ? Homestead.config.getInt("dynamic-maps.chunks.operator-color")
				: Homestead.config.getInt("dynamic-maps.chunks.color"))
				: region.getMapColor();

		areaMarker.setLineStyle(1, (double) chunkTransparencyInfill / 100, chunkColor);
		areaMarker.setFillStyle((double) chunkTransparencyOutline / 100, chunkColor);

		Placeholder placeholder = new Placeholder()
				.add("{region}", region.getName())
				.add("{region-owner}", region.getOwner().getName())
				.add("{region-members}",
						ColorTranslator.preserve(Formatter.getMembersOfRegion(region)))
				.add("{region-chunks}", region.getChunks().size())
				.add("{global-rank}", RegionManager.getGlobalRank(region.getUniqueId()))
				.add("{region-description}", region.getDescription())
				.add("{region-size}", region.getChunks().size() * 256);

		String description = Formatter
				.applyPlaceholders(isOperator ? Homestead.config.getString("dynamic-maps.chunks.operator-description")
						: Homestead.config.getString("dynamic-maps.chunks.description"), placeholder);

		areaMarker.setDescription(description);
	}

	public void update() {
		clearAllMarkers();

		for (Region region : RegionManager.getAll()) {
			for (SerializableChunk chunk : region.getChunks()) {
				addChunkMarker(region, chunk);
			}
		}
	}
}
