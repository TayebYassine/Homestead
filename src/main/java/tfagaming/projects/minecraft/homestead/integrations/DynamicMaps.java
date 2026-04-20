package tfagaming.projects.minecraft.homestead.integrations;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.integrations.maps.BlueMapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.DynmapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.Pl3xMapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.SquaremapAPI;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.IntegrationUtility;

public final class DynamicMaps {
	public static DynmapAPI dynmap;
	public static Pl3xMapAPI pl3xmap;
	public static SquaremapAPI squaremap;
	public static BlueMapAPI bluemap;

	private DynamicMaps() {
	}

	/**
	 * Apply or create new instances for each web map renderer plugin.
	 * <p>
	 * Targets: <b>dynmap</b>, <b>Pl3xMap</b>, <b>squaremap</b>, and <b>BlueMap</b>
	 * @param instance Homestead's instance
	 */
	public static void trigger(Homestead instance) {
		if (dynmap == null) {
			try {
				if (!isDynmapInstalled()) {
					throw new NoClassDefFoundError("dynmap not installed");
				}

				dynmap = new DynmapAPI(instance);

				Logger.info("Successfully connected to dynmap's API.");

				dynmap.update();
			} catch (NoClassDefFoundError ignored) {
			}
		} else {
			dynmap.update();
		}

		if (pl3xmap == null) {
			try {
				if (!isPl3xMapInstalled()) {
					throw new NoClassDefFoundError("Pl3xMap not installed");
				}

				pl3xmap = new Pl3xMapAPI(instance);

				Logger.info("Successfully connected to Pl3xMap's API.");

				pl3xmap.update();
			} catch (NoClassDefFoundError ignored) {
			}
		} else {
			pl3xmap.update();
		}

		if (squaremap == null) {
			try {
				if (!isSquaremapInstalled()) {
					throw new NoClassDefFoundError("squaremap not installed");
				}

				squaremap = new SquaremapAPI(instance);

				Logger.info("Successfully connected to Squaremap's API.");

				squaremap.update();
			} catch (NoClassDefFoundError ignored) {
			}
		} else {
			squaremap.update();
		}

		if (bluemap == null) {
			try {
				if (!isBlueMapInstalled()) {
					throw new NoClassDefFoundError("BlueMap not installed");
				}

				de.bluecolored.bluemap.api.BlueMapAPI.onEnable((api) -> {
					bluemap = new BlueMapAPI(instance, api);

					Logger.info("Successfully connected to BlueMap's API.");

					bluemap.update();
				});
			} catch (NoClassDefFoundError ignored) {
			}
		} else {
			bluemap.update();
		}

		Logger.debug("Updated dynamic map plugin markers.");
	}

	private static boolean isDynmapInstalled() {
		try {
			Class.forName("org.dynmap.markers.MarkerSet");

			return IntegrationUtility.isEnabled(IntegrationUtility.Integration.DYNMAP);
		} catch (NoClassDefFoundError | ClassNotFoundException ignored) {
			return false;
		}
	}

	private static boolean isPl3xMapInstalled() {
		try {
			Class.forName("net.pl3x.map.core.Pl3xMap");

			return IntegrationUtility.isEnabled(IntegrationUtility.Integration.PL3XMAP);
		} catch (NoClassDefFoundError | ClassNotFoundException ignored) {
			return false;
		}
	}

	private static boolean isSquaremapInstalled() {
		try {
			Class.forName("xyz.jpenilla.squaremap.api.Point");

			return IntegrationUtility.isEnabled(IntegrationUtility.Integration.SQUAREMAP);
		} catch (NoClassDefFoundError | ClassNotFoundException ignored) {
			return false;
		}
	}

	private static boolean isBlueMapInstalled() {
		try {
			Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");

			return IntegrationUtility.isEnabled(IntegrationUtility.Integration.BLUEMAP);
		} catch (NoClassDefFoundError | ClassNotFoundException ignored) {
			return false;
		}
	}
}
