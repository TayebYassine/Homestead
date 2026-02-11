package tfagaming.projects.minecraft.homestead.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.logs.Logger;

import java.io.File;
import java.util.*;

public class ConfigLoader {
	private final FileConfiguration config;

	public ConfigLoader(Homestead plugin) {
		File configFile = new File(plugin.getDataFolder(), "config.yml");

		if (!configFile.exists()) {
			Logger.error("Unable to find the configuration file, closing plugin's instance...");
			plugin.endInstance();
		}

		this.config = YamlConfiguration.loadConfiguration(configFile);

		Logger.info("The configuration file is ready.");
	}

//	@SuppressWarnings("unchecked")
//	public <T> T get(String path) {
//		return (T) config.get(path);
//	}

	public boolean getBoolean(String path, boolean defaultValue) {
		if (config == null) return defaultValue;
		return config.getBoolean(path, defaultValue);
	}

	public boolean getBoolean(String path) {
		return getBoolean(path, false);
	}

	public int getInt(String path, int defaultValue) {
		if (config == null) return defaultValue;
		return config.getInt(path, defaultValue);
	}

	public int getInt(String path) {
		return getInt(path, 0);
	}

	public long getLong(String path, long defaultValue) {
		if (config == null) return defaultValue;
		return config.getLong(path, defaultValue);
	}

	public long getLong(String path) {
		return getLong(path, 0L);
	}

	public double getDouble(String path, double defaultValue) {
		if (config == null) return defaultValue;
		return config.getDouble(path, defaultValue);
	}

	public double getDouble(String path) {
		return getDouble(path, 0.0);
	}

	public String getString(String path, String defaultValue) {
		if (config == null) return defaultValue;
		return config.getString(path, defaultValue);
	}

	public String getString(String path) {
		return getString(path, "");
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringList(String path) {
		if (config == null) return Collections.emptyList();

		if (!config.isList(path)) {
			Logger.warning("Config path '" + path + "' is not a list! Returning empty list.");
			return Collections.emptyList();
		}

		List<?> rawList = config.getList(path);
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

	/**
	 * Gets a raw object from config. Use specific getters when possible.
	 * Returns null if not found (use with caution).
	 */
	public Object getRaw(String path) {
		if (config == null) return null;
		return config.get(path);
	}

	public List<String> getKeysUnderPath(String path) {
		if (config.isConfigurationSection(path)) {
			Set<String> keys = Objects.requireNonNull(config.getConfigurationSection(path)).getKeys(false);

			return new ArrayList<String>(keys);
		}

		return new ArrayList<String>();
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public long getDefaultPlayerFlags() {
		List<String> keys = getKeysUnderPath("default-players-flags");
		long flags = 0;

		for (String key : keys) {
			boolean value = getBoolean("default-players-flags." + key);

			if (value) {
				flags = FlagsCalculator.addFlag(flags, PlayerFlags.valueOf(key));
			}
		}

		return flags;
	}

	public long getDefaultWorldFlags() {
		List<String> keys = getKeysUnderPath("default-world-flags");
		long flags = 0;

		for (String key : keys) {
			boolean value = getBoolean("default-world-flags." + key);

			if (value) {
				flags = FlagsCalculator.addFlag(flags, WorldFlags.valueOf(key));
			}
		}

		return flags;
	}

	@SuppressWarnings("unchecked")
	public boolean isFlagDisabled(String flag) {
		return getStringList("disabled-flags").contains(flag);
	}

	public boolean isWelcomeSignEnabled() {
		return getBoolean("welcome-signs.enabled");
	}

	public String getPrefix() {
		return Homestead.language.get("prefix");
	}

	public boolean isAdjacentChunksRuleEnabled() {
		return getBoolean("adjacent-chunks");
	}

	public boolean regenerateChunksWithWorldEdit() {
		return getBoolean("worldedit.regenerate-chunks");
	}

	public boolean isRewardsEnabled() {
		return getBoolean("rewards.enabled");
	}

	public boolean isLevelsEnabled() {
		return getBoolean("levels.enabled");
	}

	public boolean isDebugEnabled() {
		return getBoolean("debug");
	}
}
