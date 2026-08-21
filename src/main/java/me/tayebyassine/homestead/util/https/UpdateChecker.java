package me.tayebyassine.homestead.util.https;

import me.tayebyassine.homestead.Homestead;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public final class UpdateChecker {
	private UpdateChecker() {
		throw new AssertionError("Uninstantiable class");
	}

	/**
	 * Fetches the latest version available from the GitHub repository.
	 *
	 * @return a {@link FetchedUpdateData} record containing the current running version,
	 * the latest version from the repository, and whether an error occurred
	 */
	public static FetchedUpdateData fetch() {
		try {
			URI uri = URI.create("https://raw.githubusercontent.com/TayebYassine/Homestead/main/version.yml");
			URL url = uri.toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5_000);
			connection.setReadTimeout(5_000);

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				String response = reader.readLine();

				return new FetchedUpdateData(Homestead.getVersion(), response, false);
			}
		} catch (IOException e) {
			return new FetchedUpdateData(Homestead.getVersion(), null, true);
		}
	}

	/**
	 * Holds the result of an update check against the remote repository.
	 *
	 * @param current the version currently running
	 * @param latest  the latest version available from the repository, or {@code null} if the fetch failed
	 * @param errored {@code true} if an {@link IOException} occurred while fetching
	 */
	public record FetchedUpdateData(String current, String latest, boolean errored) {}
}
