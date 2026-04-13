package tfagaming.projects.minecraft.homestead.resources;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tfagaming.projects.minecraft.homestead.logs.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Migrate any configuration settings from the ancient {@code config.yml} (<b>v5.0.2.2</b>) to the new
 * resource files (<b>v5.1.0.0</b>).<br><br>
 * This is a temporary solution that will be removed in the future.
 */
public final class ConfigMigrator {

	private final File configFile;
	private final List<File> targetFiles;

	public ConfigMigrator(File configFile, List<File> targetFiles) {
		this.configFile = configFile;
		this.targetFiles = targetFiles;
	}

	/**
	 * Runs the migration, returns {@code true} when a change has happened.
	 *
	 * @throws IOException If saving any modified file fails.
	 */
	public boolean migrate() throws IOException {
		if (!configFile.exists()) return false;

		FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		Set<String> configTopKeys = config.getKeys(false);

		if (configTopKeys.isEmpty()) {
			return false;
		}

		List<String> globalMigratedKeys = new ArrayList<>();

		for (File targetFile : targetFiles) {
			if (!targetFile.exists()) continue;

			FileConfiguration target = YamlConfiguration.loadConfiguration(targetFile);
			List<String> migratedIntoThisFile = new ArrayList<>();

			for (String topKey : configTopKeys) {
				if (!target.contains(topKey)) continue;

				if (config.isConfigurationSection(topKey)) {
					ConfigurationSection srcSection = config.getConfigurationSection(topKey);

					ConfigurationSection dstSection = target.isConfigurationSection(topKey)
							? target.getConfigurationSection(topKey)
							: target.createSection(topKey);

					mergeSection(srcSection, dstSection);
				} else {
					target.set(topKey, config.get(topKey));
				}

				migratedIntoThisFile.add(topKey);
			}

			if (!migratedIntoThisFile.isEmpty()) {
				target.save(targetFile);
				Logger.warning("[Migrator] Migrated '" + migratedIntoThisFile + "' to '" + targetFile.getName() + "'.");
				globalMigratedKeys.addAll(migratedIntoThisFile);
			}
		}

		if (globalMigratedKeys.isEmpty()) {
			Logger.info("[Migrator] No legacy keys found in 'config.yml', nothing to migrate.");
			return false;
		}

		for (String key : globalMigratedKeys) {
			config.set(key, null);
		}

		config.save(configFile);
		Logger.info("[Migrator] Removed migrated key(s) from config.yml: " + globalMigratedKeys);

		return true;
	}

	private void mergeSection(ConfigurationSection src, ConfigurationSection dst) {
		if (src == null || dst == null) return;

		for (String key : src.getKeys(false)) {
			if (src.isConfigurationSection(key)) {
				ConfigurationSection srcChild = src.getConfigurationSection(key);
				ConfigurationSection dstChild = dst.isConfigurationSection(key)
						? dst.getConfigurationSection(key)
						: dst.createSection(key);
				mergeSection(srcChild, dstChild);
			} else {
				dst.set(key, src.get(key));
			}
		}
	}
}