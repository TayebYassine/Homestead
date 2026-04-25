package tfagaming.projects.minecraft.homestead.integrations.maps;

import com.flowpowered.math.vector.Vector2i;
import com.technicjelle.BMUtils.Cheese;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionChunk;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class BlueMapAPI extends AbstractMapIntegration {
	private static final String MARKER_SET_ID = "homestead:regions";
	private final de.bluecolored.bluemap.api.BlueMapAPI api;
	private final Map<World, MarkerSet> markerSets = new HashMap<>();

	public BlueMapAPI(Homestead plugin, de.bluecolored.bluemap.api.BlueMapAPI api) {
		super(plugin);
		this.api = api;
		update();
	}

	@Override
	public void clearAllMarkers() {
		for (BlueMapMap map : api.getMaps()) {
			MarkerSet set = map.getMarkerSets().get(MARKER_SET_ID);
			if (set != null) set.getMarkers().clear();
		}
		for (MarkerSet cached : markerSets.values()) {
			cached.getMarkers().clear();
		}
	}

	@Override
	public void update() {
		clearAllMarkers();
		for (Region region : RegionManager.getAll()) {
			addRegionMarker(region);
		}
	}

	public void addRegionMarker(Region region) {
		if (region == null || region.getOwner() == null) return;

		List<RegionChunk> chunks = ChunkManager.getChunksOfRegion(region);
		if (chunks.isEmpty()) return;

		World world = resolveRegionWorld(region);
		if (world == null) return;

		MarkerSet markerSet = getOrCreateMarkerSet(world);
		if (markerSet == null) return;

		boolean isOperator = PlayerUtility.isOperator(region.getOwner());
		String hoverText = resolveHoverText(region, isOperator)
				.replaceAll("&lt;", "<")
				.replaceAll("&gt;", ">");
		String plainLabel = resolvePlainLabel(region);
		int chunkColor = resolveChunkColor(region, isOperator);

		addAreaMarkers(markerSet, region, chunks, world, plainLabel, hoverText, chunkColor);
		addHomeMarker(world, markerSet, region, hoverText);
	}

	public MarkerSet getOrCreateMarkerSet(World world) {
		if (world == null) return null;

		MarkerSet set = markerSets.computeIfAbsent(world,
				w -> MarkerSet.builder().label(LAYER_LABEL).build());

		api.getWorld(world).ifPresent(bmWorld -> {
			for (BlueMapMap map : bmWorld.getMaps()) {
				Map<String, MarkerSet> mapSets = map.getMarkerSets();
				if (mapSets.get(MARKER_SET_ID) != set) {
					mapSets.put(MARKER_SET_ID, set);
				}
			}
		});

		return set;
	}

	private void addAreaMarkers(MarkerSet markerSet, Region region, List<RegionChunk> chunks,
								World world, String label, String hoverText, int chunkColor) {

		Vector2i[] coords = chunks.stream()
				.map(c -> new Vector2i(c.getX(), c.getZ()))
				.toArray(Vector2i[]::new);

		Collection<Cheese> platter;
		try {
			platter = Cheese.createPlatterFromChunks(coords);
		} catch (Throwable t) {
			plugin.getLogger().log(Level.WARNING,
					"BlueMap: Could not create shapes for region " + region.getName() +
							" (" + region.getUniqueId() + "): " + t.getMessage(), t);
			return;
		}

		float minY = world.getMinHeight();
		float maxY = world.getMaxHeight();
		int i = 0;

		for (Cheese cheese : platter) {
			ExtrudeMarker.Builder builder = ExtrudeMarker.builder()
					.label(label)
					.detail(hoverText)
					.shape(cheese.getShape(), minY, maxY)
					.fillColor(new Color(chunkColor, getTransparencyFill()))
					.lineColor(new Color(chunkColor, getTransparencyOutline()))
					.lineWidth(2)
					.depthTestEnabled(false);

			if (!cheese.getHoles().isEmpty()) {
				builder.holes(cheese.getHoles().toArray(Shape[]::new));
			}

			String markerId = "region-" + region.getUniqueId() + "-area-" + (i++);
			markerSet.getMarkers().put(markerId, builder.build());
		}
	}

	private void addHomeMarker(World world, MarkerSet markerSet, Region region, String hoverText) {
		if (region.getLocation() == null) return;

		Location loc = region.getLocation().toBukkit();
		if (loc == null || loc.getWorld() == null || !loc.getWorld().equals(world)) return;

		POIMarker marker = POIMarker.builder()
				.label(region.getName() + " Home")
				.detail(hoverText)
				.position(loc.getX(), loc.getY(), loc.getZ())
				.maxDistance(1000)
				.build();

		markerSet.getMarkers().put("region-" + region.getUniqueId() + "-home", marker);
	}
}