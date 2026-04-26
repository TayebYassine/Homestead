package tfagaming.projects.minecraft.homestead.integrations.maps;

import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.integrations.maps.listeners.DynmapListener;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionChunk;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

public final class DynmapAPI extends AbstractMapIntegration {

	public static MarkerSet markerSet;

	public DynmapAPI(Homestead plugin) {
		super(plugin);
		try {
			DynmapCommonAPIListener.register(new DynmapListener());
		} catch (NoClassDefFoundError ignored) {}
	}

	@Override
	public void clearAllMarkers() {
		if (markerSet == null) return;
		for (AreaMarker marker : markerSet.getAreaMarkers()) {
			marker.deleteMarker();
		}
	}

	@Override
	public void update() {
		clearAllMarkers();
		for (Region region : RegionManager.getAll()) {
			for (RegionChunk chunk : ChunkManager.getChunksOfRegion(region)) {
				addChunkMarker(region, chunk);
			}
		}
	}

	public void addChunkMarker(Region region, RegionChunk chunk) {
		if (markerSet == null || region.getOwner() == null) return;

		String markerId = buildMarkerId(chunk);
		if (markerSet.findAreaMarker(markerId) != null) return;

		AreaMarker areaMarker = markerSet.createAreaMarker(
				markerId,
				region.getName(),
				false,
				chunk.getWorld().getName(),
				new double[]{chunk.getX() * 16, (chunk.getX() + 1) * 16},
				new double[]{chunk.getZ() * 16, (chunk.getZ() + 1) * 16},
				false
		);

		if (areaMarker == null) return;

		boolean isOperator = PlayerUtility.isOperator(region.getOwner());
		int chunkColor = resolveChunkColor(region, isOperator);

		areaMarker.setLineStyle(1, (double) getTransparencyFill() / 100, chunkColor);
		areaMarker.setFillStyle((double) getTransparencyOutline() / 100, chunkColor);
		areaMarker.setDescription(resolveHoverText(region, isOperator));
	}

	private String buildMarkerId(RegionChunk chunk) {
		return "claimed_" + chunk.getWorld().getName() + "_" + chunk.getX() + "_" + chunk.getZ();
	}

	public void setMarkerSet(MarkerSet markerSet) {
		this.markerSet = markerSet;
	}
}