package tfagaming.projects.minecraft.homestead.integrations;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.integrations.maps.BlueMapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.DynmapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.Pl3xMapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.SquaremapAPI;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.IntegrationUtility;

public final class DynamicMaps {
	public static DynmapAPI DYNMAP_INSTANCE;
	public static Pl3xMapAPI PL3XMAP_INSTANCE;
	public static SquaremapAPI SQUAREMAP_INSTANCE;
	public static BlueMapAPI BLUEMAP_INSTANCE;

	private DynamicMaps() {
	}

	/**
	 * Apply or create new instances for each web map renderer plugin.
	 * <p>
	 * Targets: <b>dynmap</b>, <b>Pl3xMap</b>, <b>squaremap</b>, and <b>BlueMap</b>
	 * @param instance Homestead's instance
	 */
	public static void trigger(Homestead instance) {
		if (DYNMAP_INSTANCE == null) {
			try {
				if (!isDynmapInstalled()) {
					throw new NoClassDefFoundError("dynmap not installed");
				}

				DYNMAP_INSTANCE = new DynmapAPI(instance);

				Logger.info("[Dynamic Maps] API plugin 'dynmap' connected.");

				DYNMAP_INSTANCE.update();
			} catch (NoClassDefFoundError ignored) {
			}
		} else {
			DYNMAP_INSTANCE.update();
		}

		if (PL3XMAP_INSTANCE == null) {
			try {
				if (!isPl3xMapInstalled()) {
					throw new NoClassDefFoundError("Pl3xMap not installed");
				}

				PL3XMAP_INSTANCE = new Pl3xMapAPI(instance);

				Logger.info("[Dynamic Maps] API plugin 'Pl3xMap' connected.");

				PL3XMAP_INSTANCE.update();
			} catch (NoClassDefFoundError ignored) {
			}
		} else {
			PL3XMAP_INSTANCE.update();
		}

		if (SQUAREMAP_INSTANCE == null) {
			try {
				if (!isSquaremapInstalled()) {
					throw new NoClassDefFoundError("squaremap not installed");
				}

				SQUAREMAP_INSTANCE = new SquaremapAPI(instance);

				Logger.info("[Dynamic Maps] API plugin 'Squaremap' connected.");

				SQUAREMAP_INSTANCE.update();
			} catch (NoClassDefFoundError ignored) {
			}
		} else {
			SQUAREMAP_INSTANCE.update();
		}

		if (BLUEMAP_INSTANCE == null) {
			try {
				if (!isBlueMapInstalled()) {
					throw new NoClassDefFoundError("BlueMap not installed");
				}

				de.bluecolored.bluemap.api.BlueMapAPI.onEnable((api) -> {
					BLUEMAP_INSTANCE = new BlueMapAPI(instance, api);

					Logger.info("[Dynamic Maps] API plugin 'BlueMap' connected.");

					BLUEMAP_INSTANCE.update();
				});
			} catch (NoClassDefFoundError ignored) {
			}
		} else {
			BLUEMAP_INSTANCE.update();
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
