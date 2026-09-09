package me.tayebyassine.homestead.resources.files;

import me.tayebyassine.homestead.flags.FlagCalculator;
import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.resources.ResourceFile;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Typed accessor for {@code flags.yml}.
 */
public final class FlagsFile extends ResourceFile {

    public FlagsFile(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * Checks whether a specific flag has been disabled by the server admin.
     *
     * @param flag the flag name to check
     * @return {@code true} if the flag is in the disabled list
     */
    public boolean isFlagDisabled(String flag) {
        return getStringList("disabled-flags").contains(flag);
    }

    /**
     * Whether spawners should ignore the spawn-limit flag.
     *
     * @return {@code true} if spawners are excluded, defaults to {@code true}
     */
    public boolean doSpawnersIgnoreSpawnFlags() {
        return getBoolean("flags-configuration.exclude-spawners", true);
    }

    /**
     * Whether all flags should be automatically granted to trusted members.
     *
     * @return {@code true} if trusted players get all flags, defaults to {@code true}
     */
    public boolean allowFlagsOnPlayerTrust() {
        return getBoolean("flags-configuration.allow-all-flags-for-trusted-players", true);
    }

    /**
     * Computes the bitmask of default player flags defined in the config.
     *
     * @return combined default player flags, or {@code 0L} if none are set
     */
    public long getDefaultPlayerFlags() {
        List<String> keys = getKeysUnderPath("default-players-flags");
        long flags = 0L;

        for (String key : keys) {
            if (getBoolean("default-players-flags." + key)) {
                try {
                    flags = FlagCalculator.addFlag(flags, PlayerFlag.parse(key));
                } catch (IllegalArgumentException ignored) {
                    // Skip silently
                }
            }
        }
        return flags;
    }

    /**
     * Computes the bitmask of every player flag that is <em>not</em> disabled.
     *
     * @return combined allowed player flags
     */
    public long getAllAllowedPlayerFlagsExcludeDisabledOnes() {
        List<String> keys = PlayerFlag.getFlags();
        long flags = 0L;

        for (String key : keys) {
            if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(key)) continue;

            flags = FlagCalculator.addFlag(flags, PlayerFlag.parse(key));
        }

        return flags;
    }

    /**
     * Computes the bitmask of default world flags defined in the config.
     *
     * @return combined default world flags, or {@code 0L} if none are set
     */
    public long getDefaultWorldFlags() {
        List<String> keys = getKeysUnderPath("default-world-flags");
        long flags = 0L;

        for (String key : keys) {
            if (getBoolean("default-world-flags." + key)) {
                try {
                    flags = FlagCalculator.addFlag(flags, WorldFlag.parse(key));
                } catch (IllegalArgumentException ignored) {
                    // Skip silently
                }
            }
        }
        return flags;
    }

    /**
     * Computes the bitmask of every world flag that is <em>not</em> disabled.
     *
     * @return combined allowed world flags
     */
    public long getAllAllowedWorldFlagsExcludeDisabledOnes() {
        List<String> keys = WorldFlag.getFlags();
        long flags = 0L;

        for (String key : keys) {
            if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(key)) continue;

            flags = FlagCalculator.addFlag(flags, WorldFlag.parse(key));
        }

        return flags;
    }

    /**
     * Whether the world-rules override system is enabled.
     *
     * @return {@code true} if world rules are active, defaults to {@code false}
     */
    public boolean isWorldRulesEnabled() {
        return getBoolean("world-rules.enabled");
    }

    /**
     * The player-flags bitmask for a specific world, or {@code -1L} if the
     * world has no override.
     *
     * @param worldName the Bukkit world name
     * @return the player-flags bitmask, or {@code -1L}
     */
    public long getWorldPlayerFlags(String worldName) {
        return getLong("world-rules.worlds." + worldName + ".player_flags", -1L);
    }

    /**
     * The world-flags bitmask for a specific world, or {@code -1L} if the
     * world has no override.
     *
     * @param worldName the Bukkit world name
     * @return the world-flags bitmask, or {@code -1L}
     */
    public long getWorldWorldFlags(String worldName) {
        return getLong("world-rules.worlds." + worldName + ".world_flags", -1L);
    }
}
