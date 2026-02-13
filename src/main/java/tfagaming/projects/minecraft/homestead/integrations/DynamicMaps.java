package tfagaming.projects.minecraft.homestead.integrations;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.integrations.maps.BlueMapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.DynmapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.Pl3xMapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.SquaremapAPI;
import tfagaming.projects.minecraft.homestead.logs.Logger;

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
		if (dynmap == null && isDynmapInstalled()) {
			try {
				dynmap = new DynmapAPI(instance);

				Logger.info("Successfully connected to dynmap's API.");

				dynmap.update();
			} catch (NoClassDefFoundError ignored) {
			}
		} else {
			dynmap.update();
		}

		if (pl3xmap == null && isPl3xMapInstalled()) {
			try {
				pl3xmap = new Pl3xMapAPI(instance);

				Logger.info("Successfully connected to Pl3xMap's API.");

				pl3xmap.update();
			} catch (NoClassDefFoundError ignored) {
			}
		} else {
			pl3xmap.update();
		}

		if (squaremap == null && isSquaremapInstalled()) {
			try {
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
				Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");

				de.bluecolored.bluemap.api.BlueMapAPI.onEnable((api) -> {
					bluemap = new BlueMapAPI(instance, api);

					Logger.info("Successfully connected to BlueMap's API.");

					bluemap.update();
				});
			} catch (NoClassDefFoundError | ClassNotFoundException ignored) {
			}
		} else {
			bluemap.update();
		}

		if (Homestead.config.isDebugEnabled()) {
			Logger.info("Updated dynamic map plugin markers.");
		}
	}

	public static boolean isDynmapInstalled() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("dynmap");

		return plugin != null && plugin.isEnabled();
	}

	public static boolean isPl3xMapInstalled() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Pl3xMap");

		return plugin != null && plugin.isEnabled();
	}

	public static boolean isSquaremapInstalled() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("squaremap");

		return plugin != null && plugin.isEnabled();
	}
}
