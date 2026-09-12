package me.tayebyassine.homestead.sessions;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Manages the "target region" session for each player.
 *
 * <p>The target region is the region that subsequent actions (claim,
 * unclaim, set spawn, etc.) will operate on. Sessions are stored
 * both in memory and in the player's {@link PersistentDataContainer}
 * so they survive reconnects.
 * </p>
 */
public final class TargetRegionSession {

    public static final HashMap<UUID, Long> SESSIONS = new HashMap<UUID, Long>();
    private static final Random random = new Random();
    private static final NamespacedKey PDC_KEY = new NamespacedKey(Homestead.getInstance(), "target-region");

    private TargetRegionSession() {
        throw new AssertionError("Uninstantiable class");
    }


    /**
     * Create a new session for the given player targeting a specific
     * region.
     *
     * @param player the player
     * @param region the region to target
     */
    public static void newSession(Player player, Region region) {
        putSession(player, region.getUniqueId());
    }

    /**
     * Create a new session for the given player, automatically selecting
     * the first region they own. If they own no regions, a sentinel value
     * of {@code -1} is used.
     *
     * @param player the player
     */
    public static void newSession(Player player) {
        List<Region> regions = RegionManager.getRegionsOwnedByPlayer(player);

        if (!regions.isEmpty()) {
            putSessionIfAbsent(player, regions.getFirst().getUniqueId());
        } else {
            putSessionIfAbsent(player, -1L);
        }
    }

    /**
     * Get the target region for the given player.
     *
     * <p>If no session exists in memory, the persisted value is loaded. If
     * auto-set is enabled and the player owns regions, a random one is
     * selected.
     * </p>
     *
     * @param player the player
     * @return the target region, or {@code null} if none is set
     */
    public static Region getRegion(OfflinePlayer player) {
        Long session = SESSIONS.get(player.getUniqueId());

        if (session == null) {
            Long persisted = loadFromPersistentData(player);

            if (persisted != null) {
                session = persisted;
                SESSIONS.put(player.getUniqueId(), persisted);
            }
        }

        long regionId = session == null ? -1L : session;

        Region region = RegionManager.findRegion(regionId);

        if (region == null && Resources.<RegionsFile>get(ResourceType.Regions).isAutoSetTargetRegion() && player.isOnline() && !RegionManager.getRegionsOwnedByPlayer(player).isEmpty()) {
            randomizeRegion((Player) player);

            return getRegion(player);
        }

        return region;
    }

    /**
     * Check whether the given player has an active target-region session
     * that resolves to a valid region.
     *
     * @param player the player
     * @return {@code true} if a valid target region is set
     */
    public static boolean hasSession(Player player) {
        return SESSIONS.containsKey(player.getUniqueId()) && getRegion(player) != null;
    }

    /**
     * Set the target region for the given player.
     *
     * @param player the player
     * @param region the region to target
     */
    public static void setRegion(OfflinePlayer player, Region region) {
        putSession(player, region.getUniqueId());
    }

    /**
     * Set the target region for the given player by region name.
     *
     * @param player     the player
     * @param regionName the name of the region to target
     */
    public static void setRegion(OfflinePlayer player, String regionName) {
        Region region = RegionManager.findRegion(regionName);

        if (region == null) return;

        putSession(player, region.getUniqueId());
    }

    /**
     * Randomly select one of the regions owned by the given player and set
     * it as the target.
     *
     * @param player the player
     */
    public static void randomizeRegion(Player player) {
        List<Region> regions = RegionManager.getRegionsOwnedByPlayer(player);

        if (regions.isEmpty()) {
            putSession(player, null);
        } else {
            int randomIndex = random.nextInt(regions.size());

            setRegion(player, regions.get(randomIndex));
        }
    }

    /**
     * Remove the target-region session for the given player.
     *
     * @param player the player
     */
    public static void removeSession(Player player) {
        SESSIONS.remove(player.getUniqueId());
        saveToPersistentData(player, null);
    }

    private static void putSession(OfflinePlayer player, Long regionId) {
        SESSIONS.put(player.getUniqueId(), regionId);
        saveToPersistentData(player, regionId);
    }

    private static void putSessionIfAbsent(OfflinePlayer player, long regionId) {
        if (SESSIONS.putIfAbsent(player.getUniqueId(), regionId) == null) {
            saveToPersistentData(player, regionId);
        }
    }

    private static void saveToPersistentData(OfflinePlayer player, Long regionId) {
        Player online = player.getPlayer();
        if (online == null) return;

        PersistentDataContainer pdc = online.getPersistentDataContainer();

        if (regionId == null || regionId <= 0) {
            pdc.remove(PDC_KEY);
        } else {
            pdc.set(PDC_KEY, PersistentDataType.LONG, regionId);
        }
    }

    private static Long loadFromPersistentData(OfflinePlayer player) {
        Player online = player.getPlayer();
        if (online == null) return null;

        return online.getPersistentDataContainer().get(PDC_KEY, PersistentDataType.LONG);
    }
}
