package me.tayebyassine.homestead.resources.files;

import me.tayebyassine.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Typed accessor for {@code config.yml}.
 */
public final class ConfigFile extends ResourceFile {

    public ConfigFile(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * The active language locale code (e.g. {@code "en-US"}).
     *
     * @return the language setting, defaults to {@code "en-US"}
     */
    public String getLanguageSetting() {
        return getString("language", "en-US");
    }

    /**
     * The active menus locale code (e.g. {@code "en-US"}).
     *
     * @return the menus locale setting, defaults to {@code "en-US"}
     */
    public String getMenusSetting() {
        return getString("menus", "en-US");
    }

    /**
     * Whether the plugin debug mode is turned on.
     *
     * @return {@code true} if debug logging is enabled, defaults to {@code false}
     */
    public boolean isDebugEnabled() {
        return getBoolean("debug");
    }

    /**
     * The interval (in minutes) between cache refreshes.
     *
     * @return cache interval in minutes, defaults to {@code 30}
     */
    public int getCacheInterval() {
        return getInt("cache-interval", 30);
    }

    /**
     * The database backend provider name.
     *
     * @return provider identifier, defaults to {@code "sqlite"}
     */
    public String getDatabaseProvider() {
        return getString("database.provider", "sqlite");
    }

    /**
     * Whether FAWE chunk regeneration is enabled for region operations.
     *
     * @return {@code true} if FAWE regeneration is enabled, defaults to {@code false}
     */
    public boolean regenerateChunksWithFAWE() {
        return getBoolean("fastasyncworldedit.regenerate-chunks");
    }

    /**
     * Whether existing WorldGuard regions should be protected from claiming.
     *
     * @return {@code true} if WorldGuard protection is active, defaults to {@code false}
     */
    public boolean protectWorldGuardRegions() {
        return getBoolean("worldguard.protect-existing-regions");
    }

    /**
     * Whether the dynamic maps feature is enabled.
     *
     * @return {@code true} if dynamic maps are enabled, defaults to {@code false}
     */
    public boolean isDynamicMapsEnabled() {
        return getBoolean("dynamic-maps.enabled");
    }

    /**
     * Whether custom map icons are enabled on dynamic maps.
     *
     * @return {@code true} if icons are enabled, defaults to {@code false}
     */
    public boolean isDynamicMapsIconsEnabled() {
        return getBoolean("dynamic-maps.icons.enabled");
    }

    /**
     * The key of the default map icon.
     *
     * @return default icon key, defaults to {@code ""}
     */
    public String getDynamicMapsIconsDefault() {
        return getString("dynamic-maps.icons.default");
    }

    /**
     * The list of configured map icon keys.
     *
     * @return list of icon keys, never {@code null}
     */
    public List<String> getDynamicMapsIconKeys() {
        return getKeysUnderPath("dynamic-maps.icons.list");
    }

    /**
     * The material or identifier for a specific map icon.
     *
     * @param icon the icon key
     * @return the icon value, defaults to {@code ""}
     */
    public String getDynamicMapsIconValue(String icon) {
        return getString("dynamic-maps.icons.list." + icon);
    }

    /**
     * Whether BlueMap markers should render as 2D.
     *
     * @return {@code true} if 2D markers are used, defaults to {@code false}
     */
    public boolean isBlueMapUse2dMarkers() {
        return getBoolean("dynamic-maps.bluemap.use-2d-markers");
    }

    /**
     * Whether the Discord webhook integration is enabled.
     *
     * @return {@code true} if Discord integration is on, defaults to {@code false}
     */
    public boolean isDiscordEnabled() {
        return getBoolean("discord.enabled");
    }

    /**
     * Whether a specific Discord webhook event is enabled.
     *
     * @param eventName the event identifier (e.g. {@code "region_create"})
     * @return {@code true} if the event webhook fires, defaults to {@code false}
     */
    public boolean isDiscordEventEnabled(String eventName) {
        return getBoolean("discord.events." + eventName + ".enabled");
    }

    /**
     * The message template for a specific Discord webhook event.
     *
     * @param eventName the event identifier
     * @return the message template, defaults to {@code ""}
     */
    public String getDiscordEventMessage(String eventName) {
        return getString("discord.events." + eventName + ".message");
    }

    /**
     * The player input type used for in-game prompts.
     *
     * @return the input type string, defaults to {@code ""}
     */
    public String getPlayerInputType() {
        return getString("player-input.type");
    }

    /**
     * The default value shown by a PlaceholderAPI placeholder when no
     * region context is available.
     *
     * @param key the placeholder name (e.g. {@code "region_bank"})
     * @return the default text, defaults to {@code ""}
     */
    public String getPlaceholderDefault(String key) {
        return getString("placeholderapi.default." + key);
    }
}
