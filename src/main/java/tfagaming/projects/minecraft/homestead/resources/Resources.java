package tfagaming.projects.minecraft.homestead.resources;

import org.apache.commons.io.FileUtils;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.resources.files.*;
import tfagaming.projects.minecraft.homestead.tools.validator.ResourceValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class Resources {

	private static final String RESOURCE_CONFIG = "config.yml";
	private static final String RESOURCE_LANG_DIR = "languages/";
	private static final String RESOURCE_MENUS_DIR = "menus/";
	private static final Set<String> RESOURCE_LANGUAGE_CODES = Set.of("en-US", "es-ES", "hu-HU");
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

	public static void load(Homestead plugin) throws IOException {
		Logger.warning("Loading resources...");

		File configFile = new File(plugin.getDataFolder(), RESOURCE_CONFIG);

		if (!configFile.exists()) {
			throw new FileNotFoundException("config.yml is missing. Cannot start without it.");
		}

		validateAndFix(plugin, RESOURCE_CONFIG, configFile, Collections.emptySet());

		REGISTRY.put(ResourceType.Config, new ConfigFile(configFile));
		Logger.info("config.yml loaded.");

		ConfigFile config = get(ResourceType.Config);

		File langDir = new File(plugin.getDataFolder(), RESOURCE_LANG_DIR);
		ensureDirectoryWithDefault(plugin, langDir, RESOURCE_LANGUAGE_CODES);

		String langSetting = config.getLanguageSetting();
		File langFile = resolveLocaleFile(langDir, langSetting, "en-US.yml");

		validateAndFixDirectory(plugin, langDir, RESOURCE_LANGUAGE_CODES, Collections.emptySet());

		REGISTRY.put(ResourceType.Language, new LanguageFile(langFile));
		Logger.info("Language file '" + langFile.getName() + "' loaded.");

		File menusDir = new File(plugin.getDataFolder(), RESOURCE_MENUS_DIR);
		ensureDirectoryWithDefault(plugin, menusDir, RESOURCE_LANGUAGE_CODES);

		String menusSetting = config.getMenusSetting();
		File menusFile = resolveLocaleFile(menusDir, menusSetting, "en-US.yml");

		validateAndFixDirectory(plugin, menusDir, RESOURCE_LANGUAGE_CODES, Collections.emptySet());

		REGISTRY.put(ResourceType.Menus, new MenusFile(menusFile));
		Logger.info("Menus file '" + menusFile.getName() + "' loaded.");

		loadStandaloneResource(plugin, RESOURCE_FLAGS, ResourceType.Flags, FlagsFile::new, Collections.emptySet());
		loadStandaloneResource(plugin, RESOURCE_LEVELS, ResourceType.Levels, LevelsFile::new, Collections.emptySet());
		loadStandaloneResource(plugin, RESOURCE_LIMITS, ResourceType.Limits, LimitsFile::new, Collections.emptySet());
		loadStandaloneResource(plugin, RESOURCE_REGIONS, ResourceType.Regions, RegionsFile::new, Collections.emptySet());

		try {
			boolean migrationHappened = new ConfigMigrator(
					new File(plugin.getDataFolder(), "config.yml"),
					List.of(
							new File(plugin.getDataFolder(), "regions.yml"),
							new File(plugin.getDataFolder(), "flags.yml"),
							new File(plugin.getDataFolder(), "levels.yml"),
							new File(plugin.getDataFolder(), "limits.yml")
					)
			).migrate();

			if (migrationHappened) {
				Resources.load(plugin);
			}
		} catch (IOException e) {
			Logger.error("[Migrator] Something went terribly wrong, unable to migrate from old 'config.yml' to the new resource files!");
			Logger.error(e);
		}

		Logger.info("All resources loaded successfully.");
	}

	private static void loadStandaloneResource(
			Homestead plugin,
			String resourceName,
			ResourceType type,
			ResourceFileFactory factory,
			Set<String> filteredSubtrees) throws IOException {

		File file = new File(plugin.getDataFolder(), resourceName);
		ensureFile(plugin, file, resourceName);
		validateAndFix(plugin, resourceName, file, filteredSubtrees);

		REGISTRY.put(type, factory.create(file));
		Logger.info(resourceName + " loaded.");
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

	private static void ensureDirectoryWithDefault(Homestead plugin, File dir, Set<String> langCodes) throws IOException {
		if (dir.isDirectory()) return;

		if (!dir.mkdirs()) throw new IOException("mkdirs() returned false for " + dir);

		for (String langCode : langCodes) {
			String resourcePath = dir.getName() + "/" + langCode + ".yml";
			File file = new File(dir, langCode + ".yml");
			InputStream stream = plugin.getResource(resourcePath);

			if (stream != null) {
				FileUtils.copyInputStreamToFile(stream, file);
			}
		}
	}

	private static void ensureFile(Homestead plugin, File file, String resourceName) throws IOException {
		if (file.exists()) return;

		InputStream stream = plugin.getResource(resourceName);
		if (stream == null) {
			Logger.warning("Bundled resource '" + resourceName + "' not found in JAR.");
			return;
		}
		FileUtils.copyInputStreamToFile(stream, file);
	}

	private static void validateAndFix(
			Homestead plugin,
			String referenceResource,
			File targetFile,
			Set<String> filteredSubtrees) throws IOException {

		ResourceValidator validator = new ResourceValidator(referenceResource, targetFile, filteredSubtrees);

		if (!validator.validate()) {
			Logger.warning("'" + targetFile.getName() + "' has missing keys – auto-fixing from defaults...");
			validator.fix();
			Logger.info("'" + targetFile.getName() + "' has been repaired.");
		}
	}

	private static void validateAndFixDirectory(
			Homestead plugin,
			File dir,
			Set<String> langCodes,
			Set<String> filteredSubtrees) throws IOException {

		if (!dir.isDirectory()) return;

		for (String langCode : langCodes) {
			String resourcePath = dir.getName() + "/" + langCode + ".yml";
			File targetFile = new File(dir, langCode + ".yml");

			if (!targetFile.exists()) {
				ensureFile(plugin, targetFile, resourcePath);
			}

			ResourceValidator validator = new ResourceValidator(resourcePath, targetFile, filteredSubtrees);

			if (!validator.validate()) {
				Logger.warning("'" + targetFile.getName() + "' has missing keys – auto-fixing from defaults...");
				validator.fix();
				Logger.info("'" + targetFile.getName() + "' has been repaired.");
			}
		}
	}

	@FunctionalInterface
	private interface ResourceFileFactory {
		ResourceFile create(File file) throws FileNotFoundException;
	}
}