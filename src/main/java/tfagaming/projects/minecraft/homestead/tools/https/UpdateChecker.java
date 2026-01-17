package tfagaming.projects.minecraft.homestead.tools.https;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class UpdateChecker {
	private UpdateChecker() {
	}

	/**
	 * Fetch the latest version available from the GitHub repository.
	 * @param plugin The instance of the plugin.
	 * @return <code>true</code> if there is an update, otherwise <code>false</code>.
	 */
	public static boolean check(Homestead plugin) {
		try {
			Logger.warning("Looking for updates on GitHub...");

			URI uri = URI.create("https://raw.githubusercontent.com/TayebYassine/Homestead/main/version.yml");
			URL url = uri.toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				String response = reader.readLine();

				if (!Homestead.getVersion().equalsIgnoreCase(response)) {
					Logger.warning("There is an available update for Homestead.");
					Logger.warning("Installed: " + Homestead.getVersion() + ", Updated: " + response);
					Logger.warning(
							"Download: https://www.spigotmc.org/resources/121873/, https://modrinth.com/plugin/homestead-plugin");

					return true;
				} else {
					Logger.info("You are running on the latest version of Homestead.");

					return false;
				}
			}
		} catch (Exception e) {
			Logger.warning(
					"Failed to fetch for updates, maybe GitHub is down or you are not connected to the internet.");
			Logger.warning(
					"You can manually look for updates on SpigotMC or Modrinth: https://www.spigotmc.org/resources/121873/, https://modrinth.com/plugin/homestead-plugin");

			return false;
		}
	}
}
