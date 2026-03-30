package tfagaming.projects.minecraft.homestead.resources;

import org.apache.commons.io.FileUtils;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.resources.files.*;
import tfagaming.projects.minecraft.homestead.tools.validator.YAMLValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class Resources {

	private static final String RESOURCE_CONFIG = "config.yml";
	private static final String RESOURCE_LANG_DEFAULT = "languages/en-US.yml";
	private static final String RESOURCE_MENUS_DEFAULT = "menus/menus_en-US.yml";
	private static final String RESOURCE_FLAGS = "flags.yml";
	private static final String RESOURCE_LEVELS = "levels.yml";
	private static final String RESOURCE_LIMITS = "limits.yml";
	private static final String RESOURCE_REGIONS = "regions.yml";

	private static final Map<ResourceType, ResourceFile> REGISTRY = new EnumMap<>(ResourceType.class);

	private Resources() {
	}

	@SuppressWarnings("unchecked")
	public static <T extends ResourceFile> T get(ResourceType type) {
		ResourceFile file = REGISTRY.get(type);
		if (file == null) {
			throw new IllegalStateException(
					"Resource '" + type + "' has not been loaded yet. " +
							"Ensure Resources.load(plugin) completed before accessing resources.");
		}
		return (T) file;
	}

	public static void load(Homestead plugin) {
		Logger.info("Loading resources...");

		File configFile = new File(plugin.getDataFolder(), RESOURCE_CONFIG);

		if (!configFile.exists()) {
			Logger.error("config.yml is missing. Cannot start without it.");
			plugin.endInstance();
			return;
		}

		validateAndFix(plugin, RESOURCE_CONFIG, configFile, Collections.emptySet());

		try {
			REGISTRY.put(ResourceType.Config, new ConfigFile(configFile));
			Logger.info("config.yml loaded.");
		} catch (FileNotFoundException e) {
			Logger.error("config.yml disappeared after validation check.");
			plugin.endInstance();
			return;
		}

		ConfigFile config = get(ResourceType.Config);

		File langDir = new File(plugin.getDataFolder(), "languages/");
		ensureDirectoryWithDefault(plugin, langDir, RESOURCE_LANG_DEFAULT);

		String langSetting = config.getLanguageSetting();
		File langFile = resolveLocaleFile(langDir, langSetting, "en-US.yml");

		validateAndFix(plugin, RESOURCE_LANG_DEFAULT, langFile, Collections.emptySet());

		try {
			REGISTRY.put(ResourceType.Language, new LanguageFile(langFile));
			Logger.info("Language file '" + langFile.getName() + "' loaded.");
		} catch (FileNotFoundException e) {
			Logger.error("Language file '" + langFile.getAbsolutePath() + "' not found.");
			plugin.endInstance();
			return;
		}

		File menusDir = new File(plugin.getDataFolder(), "menus/");
		ensureDirectoryWithDefault(plugin, menusDir, RESOURCE_MENUS_DEFAULT);

		String menusSetting = config.getMenusSetting();
		File menusFile = resolveLocaleFile(menusDir, menusSetting, "menus_en-US.yml");

		validateAndFix(plugin, RESOURCE_MENUS_DEFAULT, menusFile, Collections.emptySet());

		try {
			REGISTRY.put(ResourceType.Menus, new MenusFile(menusFile));
			Logger.info("Menus file '" + menusFile.getName() + "' loaded.");
		} catch (FileNotFoundException e) {
			Logger.error("Menus file '" + menusFile.getAbsolutePath() + "' not found.");
			plugin.endInstance();
			return;
		}

		loadStandaloneResource(plugin, RESOURCE_FLAGS, ResourceType.Flags, FlagsFile::new, Collections.emptySet());
		loadStandaloneResource(plugin, RESOURCE_LEVELS, ResourceType.Levels, LevelsFile::new, Collections.emptySet());
		loadStandaloneResource(plugin, RESOURCE_LIMITS, ResourceType.Limits, LimitsFile::new, Collections.emptySet());

		loadStandaloneResource(plugin, RESOURCE_REGIONS, ResourceType.Regions, RegionsFile::new,
				Collections.singleton("regions"));

		Logger.info("All resources loaded successfully.");
	}

	private static void loadStandaloneResource(
			Homestead plugin,
			String resourceName,
			ResourceType type,
			ResourceFileFactory factory,
			Set<String> filteredSubtrees) {

		File file = new File(plugin.getDataFolder(), resourceName);
		ensureFile(plugin, file, resourceName);
		validateAndFix(plugin, resourceName, file, filteredSubtrees);

		try {
			REGISTRY.put(type, factory.create(file));
			Logger.info(resourceName + " loaded.");
		} catch (FileNotFoundException e) {
			Logger.error("Resource file '" + resourceName + "' not found after setup.");
			plugin.endInstance();
		}
	}

	private static File resolveLocaleFile(File dir, String setting, String fallbackName) {
		String name = setting.endsWith(".yml") ? setting : setting + ".yml";
		File file = new File(dir, name);
		if (!file.exists()) {
			Logger.warning("Locale file '" + name + "' not found – falling back to " + fallbackName);
			file = new File(dir, fallbackName);
		}
		return file;
	}

	private static void ensureDirectoryWithDefault(Homestead plugin, File dir, String bundledDefault) {
		if (dir.isDirectory()) return;

		try {
			if (!dir.mkdirs()) throw new IOException("mkdirs() returned false for " + dir);

			File defaultFile = new File(dir, new File(bundledDefault).getName());
			InputStream stream = plugin.getResource(bundledDefault);
			if (stream != null) {
				FileUtils.copyInputStreamToFile(stream, defaultFile);
			}
		} catch (IOException e) {
			Logger.error("Failed to create directory '" + dir + "' or copy default: " + e.getMessage());
		}
	}

	private static void ensureFile(Homestead plugin, File file, String resourceName) {
		if (file.exists()) return;

		try {
			InputStream stream = plugin.getResource(resourceName);
			if (stream == null) {
				Logger.warning("Bundled resource '" + resourceName + "' not found in JAR.");
				return;
			}
			FileUtils.copyInputStreamToFile(stream, file);
		} catch (IOException e) {
			Logger.error("Failed to copy bundled resource '" + resourceName + "': " + e.getMessage());
		}
	}

	private static void validateAndFix(
			Homestead plugin,
			String referenceResource,
			File targetFile,
			Set<String> filteredSubtrees) {

		try {
			YAMLValidator validator = new YAMLValidator(referenceResource, targetFile, filteredSubtrees);

			if (!validator.validate()) {
				Logger.warning("'" + targetFile.getName() + "' has missing keys – auto-fixing from defaults...");
				validator.fix();
				Logger.info("'" + targetFile.getName() + "' has been repaired.");
			}
		} catch (RuntimeException e) {
			Logger.warning("Cannot validate '" + targetFile.getName() + "': " + e.getMessage());
		} catch (IOException e) {
			Logger.error("Failed to auto-fix '" + targetFile.getName() + "': " + e.getMessage());
		}
	}

	@FunctionalInterface
	private interface ResourceFileFactory {
		ResourceFile create(File file) throws FileNotFoundException;
	}
}