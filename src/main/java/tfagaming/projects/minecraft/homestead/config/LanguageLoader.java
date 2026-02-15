package tfagaming.projects.minecraft.homestead.config;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LanguageLoader {
	private final Homestead plugin;
	public FileConfiguration language;

	public LanguageLoader(Homestead plugin, String language) {
		this.plugin = plugin;

		File directory = new File(plugin.getDataFolder(), "languages/");
		File defaultPath = new File(plugin.getDataFolder(), "languages/en-US.yml");

		if (!directory.isDirectory()) {
			try {
				if (!directory.mkdir()) {
					throw new IOException("Unable to create language directory");
				}

				InputStream stream = plugin.getResource("en-US.yml");

				assert stream != null;
				FileUtils.copyInputStreamToFile(stream, defaultPath);
			} catch (IOException e) {
				Logger.error("Unable to copy the default language file (en-US.yml), closing plugin's instance...");
				plugin.endInstance(e);
			}
		}

		if (language != null) {
			File localefile = new File(plugin.getDataFolder(),
					"languages/" + language + (language.endsWith(".yml") ? "" : ".yml"));

			this.language = YamlConfiguration.loadConfiguration(localefile);
		} else {
			this.language = YamlConfiguration.loadConfiguration(defaultPath);
		}

		Logger.info("The language file is ready.");
	}

	public File getLanguageFile(String language) {
		return new File(plugin.getDataFolder(),
				"languages/" + language + (language.endsWith(".yml") ? "" : ".yml"));
	}

	public String getString(String path, String defaultValue) {
		if (language == null) return defaultValue;
		return language.getString(path, defaultValue);
	}

	public String getString(String path) {
		return getString(path, "NULL @ " + path);
	}

	public List<String> getStringList(String path) {
		if (language == null) return Collections.emptyList();

		if (!language.isList(path)) {
			Logger.warning("Config path '" + path + "' is not a list! Returning empty list.");
			return Collections.emptyList();
		}

		List<?> rawList = language.getList(path);
		if (rawList == null) return Collections.emptyList();

		List<String> result = new ArrayList<>();
		for (Object obj : rawList) {
			if (obj instanceof String) {
				result.add((String) obj);
			} else {
				Logger.warning("Config path '" + path + "' contains non-string value: " + obj);
			}
		}
		return result;
	}
}
