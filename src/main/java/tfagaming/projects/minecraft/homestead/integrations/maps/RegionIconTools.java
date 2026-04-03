package tfagaming.projects.minecraft.homestead.integrations.maps;

import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionIconTools {
	private static final Map<String, BufferedImage> icons = new HashMap<>();
	private static BufferedImage defaultIcon;

	public static List<String> getAllIcons() {
		return Resources.<ConfigFile>get(ResourceType.Config).getKeysUnderPath("dynamic-maps.icons.list");
	}

	public static boolean isValidIcon(String icon) {
		return getAllIcons().contains(icon);
	}

	public static BufferedImage getIconBufferedImage(String icon) {
		if (icon == null) {
			return defaultIcon;
		}

		return icons.getOrDefault(icon, defaultIcon);
	}

	public static void downloadAllIcons() {
		List<String> allIcons = getAllIcons();
		int totalIcons = allIcons.size();
		int downloadedCount = 0;

		Logger.warning("Downloaded icons status: 0% (0 / " + totalIcons + ")");

		defaultIcon = downloadIcon(Resources.<ConfigFile>get(ResourceType.Config).getString("dynamic-maps.icons.default"));

		for (String icon : getAllIcons()) {
			if (icons.containsKey(icon)) {
				continue;
			}

			String url = Resources.<ConfigFile>get(ResourceType.Config).getString("dynamic-maps.icons.list." + icon);

			if (url != null) {
				BufferedImage downloaded = downloadIcon(url);

				icons.putIfAbsent(icon, downloaded);

				downloadedCount++;
				Logger.warning("Downloaded icons status: " + (int) ((downloadedCount / (float) totalIcons) * 100) + "% (" + downloadedCount + " / " + totalIcons + ")");
			}
		}
	}

	public static BufferedImage downloadIcon(String imageUrl) {
		try {
			URL url = new URI(imageUrl).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestProperty("User-Agent", "Mozilla/5.0");

			return ImageIO.read(connection.getInputStream());
		} catch (Exception e) {
			Logger.warning("Failed to download the icon! URL: " + imageUrl);
			return null;
		}
	}
}
