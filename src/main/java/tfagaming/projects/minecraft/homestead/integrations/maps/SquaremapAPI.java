package tfagaming.projects.minecraft.homestead.integrations.maps;

import org.bukkit.Location;
import org.bukkit.World;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionChunk;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.marker.*;
import xyz.jpenilla.squaremap.api.marker.Polygon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SquaremapAPI extends AbstractMapIntegration {

	private static final String LAYER_ID_PREFIX = "claims_";
	private final Map<World, SimpleLayerProvider> layers = new HashMap<>();
	private Squaremap api;

	public SquaremapAPI(Homestead plugin) {
		super(plugin);
		try {
			this.api = SquaremapProvider.get();
			update();
		} catch (NoClassDefFoundError ignored) {}
	}

	@Override
	public void clearAllMarkers() {
		for (SimpleLayerProvider layer : layers.values()) {
			if (layer != null) layer.getMarkers().removeIf(__ -> true);
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
		if (region.getOwner() == null) return;

		World world = chunk.getWorld();
		SimpleLayerProvider layer = getOrCreateLayer(world);
		if (layer == null) return;

		boolean isOperator = PlayerUtility.isOperator(region.getOwner());
		String hoverText = resolveHoverText(region, isOperator);
		int chunkColor = resolveChunkColor(region, isOperator);

		addChunkVisuals(layer, chunk, hoverText, chunkColor, region);

		if (getConfigBoolean("icons.enabled") && isHomeChunk(region, chunk)) {
			plugin.runAsyncTask(() -> addRegionIcon(layer, region, hoverText));
		}
	}

	private SimpleLayerProvider getOrCreateLayer(World world) {
		SimpleLayerProvider layer = layers.get(world);
		if (layer != null) return layer;

		MapWorld mapWorld = api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).orElse(null);
		if (mapWorld == null) return null;

		layer = SimpleLayerProvider.builder(LAYER_LABEL)
				.showControls(true)
				.defaultHidden(false)
				.layerPriority(1)
				.zIndex(250)
				.build();

		mapWorld.layerRegistry().register(Key.of(LAYER_ID_PREFIX + world.getName()), layer);
		layers.put(world, layer);
		return layer;
	}

	private void addChunkVisuals(SimpleLayerProvider layer, RegionChunk chunk, String hoverText,
								 int chunkColor, Region region) {
		boolean north = !isNeighborClaimed(region, chunk, GeoDirection.NORTH);
		boolean east  = !isNeighborClaimed(region, chunk, GeoDirection.EAST);
		boolean south = !isNeighborClaimed(region, chunk, GeoDirection.SOUTH);
		boolean west  = !isNeighborClaimed(region, chunk, GeoDirection.WEST);

		Key chunkKey = Key.of("chunk_" + chunk.getX() + "_" + chunk.getZ());
		addFillPolygon(layer, chunk, chunkKey, hoverText, chunkColor);

		MarkerOptions borderOpts = MarkerOptions.builder()
				.strokeColor(applyAlpha(chunkColor, 255))
				.strokeWeight(2)
				.stroke(true)
				.fill(false)
				.build();

		if (north) addBorderLine(layer, chunkKey, "north", buildPoints(chunk, GeoDirection.NORTH), borderOpts);
		if (east)  addBorderLine(layer, chunkKey, "east", buildPoints(chunk, GeoDirection.EAST), borderOpts);
		if (south) addBorderLine(layer, chunkKey, "south", buildPoints(chunk, GeoDirection.SOUTH), borderOpts);
		if (west)  addBorderLine(layer, chunkKey, "west", buildPoints(chunk, GeoDirection.WEST), borderOpts);
	}

	private void addFillPolygon(SimpleLayerProvider layer, RegionChunk chunk, Key chunkKey,
								String hoverText, int chunkColor) {
		double minX = chunk.getX() * 16.0;
		double minZ = chunk.getZ() * 16.0;
		double maxX = minX + 16.0;
		double maxZ = minZ + 16.0;

		Polygon fill = Marker.polygon(List.of(
				Point.of(minX, minZ),
				Point.of(maxX, minZ),
				Point.of(maxX, maxZ),
				Point.of(minX, maxZ),
				Point.of(minX, minZ)
		));

		fill.markerOptions(MarkerOptions.builder()
				.hoverTooltip(hoverText)
				.fillColor(applyAlpha(chunkColor, getTransparencyFill()))
				.strokeColor(applyAlpha(chunkColor, getTransparencyOutline()))
				.strokeWeight(0)
				.fill(true)
				.stroke(false)
				.build());

		layer.addMarker(Key.of(chunkKey + "_fill"), fill);
	}

	private List<Point> buildPoints(RegionChunk chunk, GeoDirection side) {
		double minX = chunk.getX() * 16.0;
		double minZ = chunk.getZ() * 16.0;
		double maxX = minX + 16.0;
		double maxZ = minZ + 16.0;

		return switch (side) {
			case NORTH -> List.of(Point.of(minX, minZ), Point.of(maxX, minZ));
			case EAST  -> List.of(Point.of(maxX, minZ), Point.of(maxX, maxZ));
			case SOUTH -> List.of(Point.of(minX, maxZ), Point.of(maxX, maxZ));
			case WEST  -> List.of(Point.of(minX, minZ), Point.of(minX, maxZ));
		};
	}

	private void addBorderLine(SimpleLayerProvider layer, Key chunkKey, String side,
							   List<Point> points, MarkerOptions options) {
		Polyline line = Marker.polyline(points);
		line.markerOptions(options);
		layer.addMarker(Key.of(chunkKey + "_" + side), line);
	}

	private void addRegionIcon(SimpleLayerProvider layer, Region region, String hoverText) {
		if (region.getLocation() == null) return;

		BufferedImage icon = RegionIcon.getIconBufferedImage(region.getMapIcon());
		if (icon == null) return;

		try {
			Key iconKey = Key.of("region_icon_" + region.getName().toLowerCase().replaceAll(" ", "_"));
			Location loc = region.getLocation().toBukkit();
			if (loc == null) return;

			Point point = Point.of(loc.getX(), loc.getZ());

			if (api.iconRegistry().hasEntry(iconKey) && !api.iconRegistry().get(iconKey).equals(icon)) {
				api.iconRegistry().unregister(iconKey);
			}
			if (!api.iconRegistry().hasEntry(iconKey)) {
				api.iconRegistry().register(iconKey, icon);
			}

			Icon marker = Marker.icon(point, iconKey, getConfigInt("icons.size"), getConfigInt("icons.size"));
			marker.markerOptions(MarkerOptions.builder().hoverTooltip(hoverText).build());

			layer.addMarker(iconKey, marker);
		} catch (Exception ignored) {}
	}

	private boolean isHomeChunk(Region region, RegionChunk chunk) {
		if (region.getLocation() == null) return false;

		double locX = region.getLocation().getX();
		double locZ = region.getLocation().getZ();
		int cx = (int) Math.floor(locX) >> 4;
		int cz = (int) Math.floor(locZ) >> 4;

		return cx == chunk.getX() && cz == chunk.getZ()
				&& region.getLocation().getWorldId().equals(chunk.getWorldId());
	}

	private Color applyAlpha(int color, int alpha) {
		return new Color(
				(color >> 16) & 0xFF,
				(color >> 8) & 0xFF,
				color & 0xFF,
				alpha);
	}
}