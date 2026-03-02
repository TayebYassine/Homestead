package tfagaming.projects.minecraft.homestead.integrations.faststats;

import dev.faststats.bukkit.BukkitMetrics;
import tfagaming.projects.minecraft.homestead.Homestead;

public class Metrics {
	private final dev.faststats.core.Metrics metrics;

	public Metrics(Homestead plugin) {
		metrics = BukkitMetrics.factory()
				.token("f8be7c43060ca8494ee5c93b3e551261")
				.create(plugin);
	}
}
