package me.tayebyassine.homestead.resources.files;

import me.tayebyassine.homestead.resources.ResourceFile;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;

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

	public boolean regenerateChunksWithFAWE() {
		return getBoolean("fastasyncworldedit.regenerate-chunks");
	}

	public boolean protectWorldGuardRegions() {
		return getBoolean("worldguard.protect-existing-regions");
	}

	public int getCacheInterval() {
		return getInt("cache-interval", 30);
	}

	public String getDatabaseProvider() {
		return getString("database.provider", "sqlite");
	}
}