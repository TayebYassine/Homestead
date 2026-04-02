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

	public boolean isMetricsEnabled() {
		return getBoolean("metrics", true);
	}

	public boolean isAutoSetTargetRegionEnabled() {
		return getBoolean("autoset-target-region", true);
	}

	public double getChunkPrice() {
		return getDouble("chunk-price", 0.0);
	}

	public boolean isCleanStartupEnabled() {
		return getBoolean("clean-startup", true);
	}

	public boolean isLogPrivateChatEnabled() {
		return getBoolean("log-private-chat", true);
	}

	public boolean isWelcomeSignEnabled() {
		return getBoolean("welcome-signs.enabled");
	}

	public boolean isSubAreasEnabled() {
		return getBoolean("sub-areas.enabled", true);
	}

	public boolean isWarsEnabled() {
		return getBoolean("wars.enabled");
	}

	public boolean isUpkeepEnabled() {
		return getBoolean("upkeep.enabled");
	}

	public boolean isTaxesEnabled() {
		return getBoolean("taxes.enabled");
	}

	public boolean isRentingEnabled() {
		return getBoolean("renting.enabled", true);
	}

	public boolean isSellingEnabled() {
		return getBoolean("selling.enabled", true);
	}

	public boolean isDynamicMapsEnabled() {
		return getBoolean("dynamic-maps.enabled", true);
	}

	public boolean regenerateChunksWithWorldEdit() {
		return getBoolean("worldedit.regenerate-chunks");
	}

	public boolean isWorldGuardProtectionEnabled() {
		return getBoolean("worldguard.protect-existing-regions", true);
	}

	public boolean isDelayedTeleportEnabled() {
		return getBoolean("delayed-teleport.enabled", true);
	}

	public int getTeleportDelay() {
		return getInt("delayed-teleport.delay", 3);
	}

	public double getTeleportPrice() {
		return getDouble("delayed-teleport.price", 0.0);
	}

	public boolean cancelTeleportOnMove() {
		return getBoolean("delayed-teleport.cancel-on-move", true);
	}

	public boolean operatorsIgnoreTeleportDelay() {
		return getBoolean("delayed-teleport.ignore-operators", true);
	}

	public boolean isTntOnlyBelowSeaLevelEnabled() {
		return getBoolean("special-feat.tnt-explodes-only-below-sea-level");
	}

	public boolean ignoreRegionProtectionInDisabledWorlds() {
		return getBoolean("special-feat.ignore-region-protection-if-action-in-disabled-world", true);
	}

	public int getCacheInterval() {
		return getInt("cache-interval", 30);
	}

	public String getDatabaseProvider() {
		return getString("database.provider", "sqlite");
	}
}