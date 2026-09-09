package me.tayebyassine.homestead.resources.files;

import me.tayebyassine.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Typed accessor for {@code limits.yml}.
 */
public final class LimitsFile extends ResourceFile {

    public LimitsFile(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * The limit resolution method used by the plugin.
     * <p>
     * Valid values are {@code "static"}, {@code "groups"}, or
     * {@code "permissions"}.
     *
     * @return the method name, defaults to {@code "static"}
     */
    public String getLimitsMethod() {
        return getString("limits.method", "static");
    }

    /**
     * A player-specific limit override, or {@code null} if the player has
     * no override for this limit.
     *
     * @param playerName the player name
     * @param limitKey   the limit config key (e.g. {@code "regions"})
     * @return the override value, or {@code null}
     */
    public Object getPlayerLimit(String playerName, String limitKey) {
        return getRaw("player-limits." + playerName + "." + limitKey);
    }

    /**
     * A static limit value for op or non-op players.
     *
     * @param isOp     whether the player is a server operator
     * @param limitKey the limit config key
     * @return the static limit, or {@code null} if not set
     */
    public Object getStaticLimit(boolean isOp, String limitKey) {
        String opKey = isOp ? "op" : "non-op";
        return getRaw("limits.static." + opKey + "." + limitKey);
    }

    /**
     * A group-based limit value.
     *
     * @param group    the permission group name
     * @param limitKey the limit config key
     * @return the group limit, or {@code null} if not set
     */
    public Object getGroupLimit(String group, String limitKey) {
        return getRaw("limits.groups." + group + "." + limitKey);
    }

    /**
     * The ordered list of permission-group priority keys.
     *
     * @return priority group names, never {@code null}
     */
    public List<String> getPermissionsPriority() {
        return getStringList("limits.permissions-priority");
    }

    /**
     * The set of permission group keys defined in the config.
     *
     * @return permission group names, or an empty set if the section is missing
     */
    public Set<String> getPermissionGroupKeys() {
        if (getConfig().isConfigurationSection("limits.permissions")) {
            return Objects.requireNonNull(getConfig().getConfigurationSection("limits.permissions"))
                    .getKeys(false);
        }
        return Set.of();
    }

    /**
     * A permission-based limit value for a specific group.
     *
     * @param group    the permission group name
     * @param limitKey the limit config key
     * @return the permission limit, or {@code null} if not set
     */
    public Object getPermissionLimit(String group, String limitKey) {
        return getRaw("limits.permissions." + group + "." + limitKey);
    }
}
