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

public final class RegionIcon {
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
		int downloaded = 0;

		Logger.info("[Dynamic Maps] Downloading icons... 0% (0 / " + totalIcons + ")");

		defaultIcon = downloadIcon(Resources.<ConfigFile>get(ResourceType.Config).getString("dynamic-maps.icons.default"));

		for (String icon : getAllIcons()) {
			if (icons.containsKey(icon)) {
				continue;
			}

			String url = Resources.<ConfigFile>get(ResourceType.Config).getString("dynamic-maps.icons.list." + icon);

			if (url != null) {
				BufferedImage bufferedImage = downloadIcon(url);

				icons.putIfAbsent(icon, bufferedImage);

				downloaded++;
				Logger.info("[Dynamic Maps] Downloading icons... " + (int) ((downloaded / (float) totalIcons) * 100) + "% (" + downloaded + " / " + totalIcons + ")");
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
			Logger.warning("[Dynamic Maps] Unable to download an icon, URL: " + imageUrl);
			return null;
		}
	}
}
