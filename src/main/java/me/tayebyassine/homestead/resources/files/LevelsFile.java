package me.tayebyassine.homestead.resources.files;

import me.tayebyassine.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Typed accessor for {@code levels.yml}.
 */
public final class LevelsFile extends ResourceFile {

    public LevelsFile(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * Whether the region leveling system is enabled.
     *
     * @return {@code true} if leveling is active, defaults to {@code false}
     */
    public boolean isEnabled() {
        return getBoolean("levels.enabled");
    }

    /**
     * The timeout (in minutes) between level-up reward notifications.
     *
     * @return timeout in minutes, defaults to {@code 0} (no cooldown)
     */
    public int getTimeout() {
        return getInt("levels.timeout", 0);
    }

    /**
     * The minimum XP awarded for killing a specific entity type.
     *
     * @param entityType the Bukkit entity type name
     * @return minimum XP, defaults to {@code 0}
     */
    public int getKillXpMin(String entityType) {
        return getIntegerList("levels.on-kill-entity." + entityType)
                .stream().findFirst().orElse(0);
    }

    /**
     * The maximum XP awarded for killing a specific entity type.
     *
     * @param entityType the Bukkit entity type name
     * @return maximum XP, defaults to the min value if only one entry exists
     */
    public int getKillXpMax(String entityType) {
        java.util.List<Integer> range = getIntegerList("levels.on-kill-entity." + entityType);
        return range.size() >= 2 ? range.get(1) : range.stream().findFirst().orElse(0);
    }

    /**
     * The chunk-reward bonus for reaching a specific level.
     *
     * @param level the region level
     * @return bonus chunks, defaults to {@code 0}
     */
    public int getRewardChunks(int level) {
        return getInt("levels.rewards." + level + ".chunks", 0);
    }

    /**
     * The member-reward bonus for reaching a specific level.
     *
     * @param level the region level
     * @return bonus members, defaults to {@code 0}
     */
    public int getRewardMembers(int level) {
        return getInt("levels.rewards." + level + ".members", 0);
    }

    /**
     * The sub-area-reward bonus for reaching a specific level.
     *
     * @param level the region level
     * @return bonus sub-areas, defaults to {@code 0}
     */
    public int getRewardSubAreas(int level) {
        return getInt("levels.rewards." + level + ".subareas", 0);
    }

    /**
     * The upkeep-reduction percentage for reaching a specific level.
     *
     * @param level the region level
     * @return upkeep-reduction percentage, defaults to {@code 0}
     */
    public int getRewardUpkeepReduction(int level) {
        return getInt("levels.rewards." + level + ".upkeep-reduction", 0);
    }
}
