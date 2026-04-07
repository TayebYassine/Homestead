package tfagaming.projects.minecraft.homestead.resources.files;

import tfagaming.projects.minecraft.homestead.resources.ResourceFile;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;

import java.io.File;
import java.io.FileNotFoundException;

public class ConfigFile extends ResourceFile {

	public ConfigFile(File file) throws FileNotFoundException {
		super(file);
	}

	public String getLanguageSetting() {
		return getString("language", "en-US");
	}

	public String getMenusSetting() {
		return getString("menus", "en-US");
	}

	public String getPrefix() {
		return Resources.<LanguageFile>get(ResourceType.Language).getString("prefix");
	}

	public boolean isDebugEnabled() {
		return getBoolean("debug");
	}

	public boolean regenerateChunksWithChunky() {
		return getBoolean("chunky.regenerate-chunks");
	}

	public int getCacheInterval() {
		return getInt("cache-interval", 30);
	}

	public String getDatabaseProvider() {
		return getString("database.provider", "sqlite");
	}
}