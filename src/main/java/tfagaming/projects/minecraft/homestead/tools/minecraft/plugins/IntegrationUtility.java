package tfagaming.projects.minecraft.homestead.tools.minecraft.plugins;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class IntegrationUtility {
	private IntegrationUtility() {
	}

	public static Plugin getInstance(Integration plugin) {
		return Bukkit.getServer().getPluginManager().getPlugin(plugin.getName());
	}

	public static boolean isEnabled(Integration plugin) {
		Plugin instance = getInstance(plugin);

		return instance != null && instance.isEnabled();
	}

	public enum Integration {
		VAULT("Vault"),
		PAPI("PlaceholderAPI"),
		DYNMAP("dynmap"),
		PL3XMAP("Pl3xMap"),
		SQUAREMAP("squaremap"),
		BLUEMAP("BlueMap"),
		NEXO("Nexo");

		private final String name;

		Integration(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}
}
