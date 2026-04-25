package tfagaming.projects.minecraft.homestead.integrations.maps;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Polyline;
import net.pl3x.map.core.markers.marker.Rectangle;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.registry.Registry;
import net.pl3x.map.core.util.Colors;
import org.bukkit.Location;
import org.bukkit.World;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionChunk;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Pl3xMapAPI extends AbstractMapIntegration {

	private final Map<World, SimpleLayer> layers = new HashMap<>();

	public Pl3xMapAPI(Homestead plugin) {
		super(plugin);
		try {
			update();
		} catch (NoClassDefFoundError ignored) {}
	}

	@Override
	public void clearAllMarkers() {
		for (SimpleLayer layer : layers.values()) {
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
		SimpleLayer layer = getOrCreateLayer(world);
		if (layer == null) return;

		boolean isOperator = PlayerUtility.isOperator(region.getOwner());
		String hoverText = resolveHoverText(region, isOperator);
		int chunkColor = resolveChunkColor(region, isOperator);

		addChunkVisuals(layer, chunk, hoverText, chunkColor, region);

		if (getConfigBoolean("icons.enabled") && isHomeChunk(region, chunk)) {
			plugin.runAsyncTask(() -> addRegionIcon(layer, region, hoverText));
		}
	}

	private SimpleLayer getOrCreateLayer(World world) {
		SimpleLayer layer = layers.get(world);
		if (layer != null) return layer;

		String layerId = "claims_" + world.getName();
		Registry<net.pl3x.map.core.world.@org.jetbrains.annotations.NotNull World> registry = Pl3xMap.api().getWorldRegistry();
		net.pl3x.map.core.world.World mapWorld = registry.get(world.getName());

		if (mapWorld == null) return null;

		layer = new SimpleLayer(layerId, () -> LAYER_LABEL);
		layer.setPriority(1);
		layer.setZIndex(1);
		layer.setLiveUpdate(true);

		mapWorld.getLayerRegistry().register(layer);
		layers.put(world, layer);
		return layer;
	}

	private void addChunkVisuals(SimpleLayer layer, RegionChunk chunk, String hoverText,
								 int chunkColor, Region region) {
		boolean north = !isNeighborClaimed(region, chunk, GeoDirection.NORTH);
		boolean east  = !isNeighborClaimed(region, chunk, GeoDirection.EAST);
		boolean south = !isNeighborClaimed(region, chunk, GeoDirection.SOUTH);
		boolean west  = !isNeighborClaimed(region, chunk, GeoDirection.WEST);

		addFillRectangle(layer, chunk, hoverText, chunkColor);
		if (north) addBorderLine(layer, chunk, GeoDirection.NORTH, chunkColor);
		if (east)  addBorderLine(layer, chunk, GeoDirection.EAST, chunkColor);
		if (south) addBorderLine(layer, chunk, GeoDirection.SOUTH, chunkColor);
		if (west)  addBorderLine(layer, chunk, GeoDirection.WEST, chunkColor);
	}

	private void addFillRectangle(SimpleLayer layer, RegionChunk chunk, String hoverText, int chunkColor) {
		String markerId = "fill_" + chunk.getX() + "_" + chunk.getZ();
		Point p1 = Point.of(chunk.getX() * 16, chunk.getZ() * 16);
		Point p2 = Point.of(chunk.getX() * 16 + 16, chunk.getZ() * 16 + 16);

		Rectangle rect = new Rectangle(markerId, p1, p2);
		rect.setOptions(Options.builder()
				.tooltipContent(hoverText)
				.fillColor(Colors.setAlpha(getTransparencyFill(), chunkColor))
				.strokeColor(Colors.setAlpha(getTransparencyOutline(), chunkColor))
				.strokeWeight(0)
				.fill(true)
				.stroke(false)
				.build());

		layer.addMarker(rect);
	}

	private void addBorderLine(SimpleLayer layer, RegionChunk chunk, GeoDirection side, int color) {
		int x = chunk.getX(), z = chunk.getZ();
		String markerId = "border_" + x + "_" + z + "_" + side;

		Point p1, p2;
		switch (side) {
			case NORTH -> { p1 = Point.of(x * 16, z * 16);     p2 = Point.of(x * 16 + 16, z * 16); }
			case EAST  -> { p1 = Point.of(x * 16 + 16, z * 16); p2 = Point.of(x * 16 + 16, z * 16 + 16); }
			case SOUTH -> { p1 = Point.of(x * 16, z * 16 + 16); p2 = Point.of(x * 16 + 16, z * 16 + 16); }
			case WEST  -> { p1 = Point.of(x * 16, z * 16);     p2 = Point.of(x * 16, z * 16 + 16); }
			default -> { return; }
		}

		Polyline line = new Polyline(markerId, List.of(p1, p2));
		line.setOptions(Options.builder()
				.strokeColor(Colors.setAlpha(255, color))
				.strokeWeight(2)
				.build());

		layer.addMarker(line);
	}

	private void addRegionIcon(SimpleLayer layer, Region region, String hoverText) {
		if (region.getLocation() == null) return;

		BufferedImage icon = RegionIcon.getIconBufferedImage(region.getMapIcon());
		if (icon == null) return;

		try {
			String iconId = "region_icon_" + region.getName().toLowerCase().replaceAll(" ", "_");
			Location loc = region.getLocation().toBukkit();
			if (loc == null) return;

			Point point = Point.of(loc.getX(), loc.getZ());
			IconImage iconImage = new IconImage(iconId, icon, "png");

			var iconRegistry = Pl3xMap.api().getIconRegistry();
			if (iconRegistry.has(iconId) && !Objects.requireNonNull(iconRegistry.get(iconId)).getImage().equals(iconImage.getImage())) {
				iconRegistry.unregister(iconId);
			}
			if (!iconRegistry.has(iconId)) {
				iconRegistry.register(iconId, iconImage);
			}

			Marker<?> iconMarker = Marker.icon("marker_" + iconId, point, iconId,
					getConfigInt("icons.size"), getConfigInt("icons.size"));
			iconMarker.setOptions(Options.builder().tooltipContent(hoverText).build());

			layer.addMarker(iconMarker);
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
}