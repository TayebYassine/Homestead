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

public final class TargetRegionSession {
    public static final HashMap<UUID, Long> SESSIONS = new HashMap<UUID, Long>();
    private static final Random random = new Random();
    private static final NamespacedKey PDC_KEY = new NamespacedKey(Homestead.getInstance(), "target-region");

    private TargetRegionSession() {
        throw new AssertionError("Uninstantiable class");
    }

    public static void newSession(Player player, Region region) {
        putSession(player, region.getUniqueId());
    }

    public static void newSession(Player player) {
        List<Region> regions = RegionManager.getRegionsOwnedByPlayer(player);

        if (!regions.isEmpty()) {
            putSessionIfAbsent(player, regions.getFirst().getUniqueId());
        } else {
            putSessionIfAbsent(player, -1L);
        }
    }

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

        if (region == null && Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("autoset-target-region") && player.isOnline() && !RegionManager.getRegionsOwnedByPlayer(player).isEmpty()) {
            randomizeRegion((Player) player);

            return getRegion(player);
        }

        return region;
    }

    public static void setRegion(OfflinePlayer player, Region region) {
        putSession(player, region.getUniqueId());
    }

    public static void setRegion(OfflinePlayer player, String regionName) {
        Region region = RegionManager.findRegion(regionName);

        if (region == null) return;

        putSession(player, region.getUniqueId());
    }

    public static void randomizeRegion(
            Player player) {
        List<Region> regions = RegionManager.getRegionsOwnedByPlayer(player);

        if (regions.isEmpty()) {
            putSession(player, null);
        } else {
            int randomIndex = random.nextInt(regions.size());

            setRegion(player, regions.get(randomIndex));
        }
    }

    public static boolean hasSession(Player player) {
        return SESSIONS.containsKey(player.getUniqueId()) && getRegion(player) != null;
    }

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
