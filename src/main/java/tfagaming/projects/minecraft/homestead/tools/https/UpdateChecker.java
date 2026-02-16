package tfagaming.projects.minecraft.homestead.tools.https;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public final class UpdateChecker {
	private UpdateChecker() {
	}

	/**
	 * Fetch the latest version available from the GitHub repository.
	 * @param plugin The instance of the plugin.
	 * @return <code>true</code> if there is an update, otherwise <code>false</code>.
	 */
	public static boolean fetch(Homestead plugin) {
		try {
			Logger.warning("Looking for updates on GitHub...");

			URI uri = URI.create("https://raw.githubusercontent.com/TayebYassine/Homestead/main/version.yml");
			URL url = uri.toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				String response = reader.readLine();

				if (!Homestead.getVersion().equalsIgnoreCase(response)) {
					Logger.warning(Logger.PredefinedMessages.UPDATE_FOUND.getMessage());

					return true;
				} else {
					Logger.info("You are running on the latest version of Homestead.");

					return false;
				}
			}
		} catch (Exception e) {
			Logger.error(Logger.PredefinedMessages.UPDATE_FETCH_FAILURE.getMessage());

			return false;
		}
	}
}
