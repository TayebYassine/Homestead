package me.tayebyassine.homestead.integrations.faststats;

import dev.faststats.bukkit.BukkitContext;
import me.tayebyassine.homestead.Homestead;

public class Metrics {
	public Metrics(Homestead plugin) {
		BukkitContext context = new BukkitContext.Factory(plugin, "f8be7c43060ca8494ee5c93b3e551261")
				.metrics(dev.faststats.Metrics.Factory::create)
				.create();

		context.ready();
	}
}
