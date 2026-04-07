package tfagaming.projects.minecraft.homestead.resources;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ResourceFile {

	private final FileConfiguration config;

	public ResourceFile(File file) throws FileNotFoundException {
		if (!file.exists()) {
			throw new FileNotFoundException("Resource file not found: " + file.getAbsolutePath());
		}
		this.config = YamlConfiguration.loadConfiguration(file);
	}

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

	public float getFloat(String path, float defaultValue) {
		if (config == null) return defaultValue;
		return (float) config.getDouble(path, defaultValue);
	}

	public float getFloat(String path) {
		return getFloat(path, 0.0F);
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

	public List<String> getStringList(String path) {
		if (config == null) return Collections.emptyList();
		if (!config.isList(path)) return Collections.emptyList();

		List<?> raw = config.getList(path);
		if (raw == null) return Collections.emptyList();

		List<String> result = new ArrayList<>();
		for (Object obj : raw) {
			if (obj instanceof String) result.add((String) obj);
		}
		return result;
	}

	public List<Integer> getIntegerList(String path) {
		if (config == null) return Collections.emptyList();
		if (!config.isList(path)) return Collections.emptyList();

		List<?> raw = config.getList(path);
		if (raw == null) return Collections.emptyList();

		List<Integer> result = new ArrayList<>();
		for (Object obj : raw) {
			if (obj instanceof Integer) result.add((Integer) obj);
		}
		return result;
	}

	/**
	 * Returns every direct child key of the given configuration section,
	 * or an empty list if the path is not a section.
	 */
	public List<String> getKeysUnderPath(String path) {
		if (!config.isConfigurationSection(path)) return Collections.emptyList();
		Set<String> keys = Objects.requireNonNull(config.getConfigurationSection(path)).getKeys(false);
		return new ArrayList<>(keys);
	}

	public Object getRaw(String path) {
		if (config == null) return null;
		return config.get(path);
	}

	public FileConfiguration getConfig() {
		return config;
	}
}