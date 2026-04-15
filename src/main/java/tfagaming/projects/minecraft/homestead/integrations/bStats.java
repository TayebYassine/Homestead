package tfagaming.projects.minecraft.homestead.integrations;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.database.Driver;
import tfagaming.projects.minecraft.homestead.integrations.bstats.Metrics;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.structure.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public final class bStats {
	public bStats(Homestead plugin) {
		try {
			Metrics metrics = new Metrics(plugin, 25286);

			// Regions
			metrics.addCustomChart(new Metrics.SingleLineChart("regions", new Callable<Integer>() {
				@Override
				public Integer call() {
					return RegionManager.getAll().size();
				}
			}));

			// Sub-Areas
			metrics.addCustomChart(new Metrics.SingleLineChart("subareas", new Callable<Integer>() {
				@Override
				public Integer call() {
					return SubAreaManager.getAll().size();
				}
			}));

			// Trusted Players
			metrics.addCustomChart(new Metrics.SingleLineChart("trusted_players", new Callable<Integer>() {
				@Override
				public Integer call() {
					int players = 0;

					for (Region region : RegionManager.getAll()) {
						players += region.getMembers().size();
					}

					return players;
				}
			}));

			// Chunks
			metrics.addCustomChart(new Metrics.SingleLineChart("chunks", new Callable<Integer>() {
				@Override
				public Integer call() {
					int chunks = 0;

					for (Region region : RegionManager.getAll()) {
						chunks += region.getChunks().size();
					}

					return chunks;
				}
			}));

			// Database
			metrics.addCustomChart(new Metrics.AdvancedPie("database_provider", new Callable<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> call() {
					Map<String, Integer> map = new HashMap<>();

					Driver provider = Homestead.database.getProvider();

					if (provider != null) {
						map.put(provider.toString(), 1);
					}

					return map;
				}
			}));

			// Dynamic Maps
			metrics.addCustomChart(new Metrics.AdvancedPie("dynamic_maps", new Callable<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> call() {
					Map<String, Integer> map = new HashMap<>();

					map.put("Dynmap", DynamicMaps.dynmap == null ? 0 : 1);
					map.put("Squaremap", DynamicMaps.squaremap == null ? 0 : 1);
					map.put("Pl3xMap", DynamicMaps.pl3xmap == null ? 0 : 1);
					map.put("BlueMap", DynamicMaps.bluemap == null ? 0 : 1);

					return map;
				}
			}));

			// Wars
			metrics.addCustomChart(new Metrics.SingleLineChart("wars", new Callable<Integer>() {
				@Override
				public Integer call() {
					return WarManager.getAll().size();
				}
			}));

		} catch (NoClassDefFoundError | IllegalStateException e) {
			Logger.error("Unable to communicate with bStats servers.");
		}
	}
}
