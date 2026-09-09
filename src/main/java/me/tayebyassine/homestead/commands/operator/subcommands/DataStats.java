package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.database.Database;
import me.tayebyassine.homestead.managers.*;
import org.bukkit.Bukkit;

import java.util.Arrays;

/**
 * Builds the "property / value" tables printed by the admin sub-commands, so that the
 * underlying data summary is defined in a single place.
 */
public final class DataStats {

    private DataStats() {
    }

    /**
     * @return rows describing the server software and the plugin's database status
     */
    public static Object[][] infoRows() {
        long databaseLatency = Homestead.database.getLatency();
        long cacheLatency = Database.getCacheLatency();

        return new Object[][]{
                {"Software", Bukkit.getName()},
                {"Version", Bukkit.getVersion()},
                {"Players", Bukkit.getOnlinePlayers().size()},
                {"Homestead", "v" + Homestead.getVersion()},
                {"Database Provider", Homestead.database.getProvider().toString()},
                {"Database Latency", databaseLatency + " ms (" + getLatencyStatus(databaseLatency) + ")"},
                {"Cache Latency", cacheLatency + " ms (" + getLatencyStatus(cacheLatency) + ")"},
        };
    }

    /**
     * @return rows describing the amount of data stored for each model
     */
    public static Object[][] dataRows() {
        return new Object[][]{
                {"Regions", RegionManager.getRegionCount()},
                {"Members", MemberManager.getMemberCount()},
                {"Chunks", ChunkManager.getChunkCount()},
                {"Invites", InviteManager.getInviteCount()},
                {"Logs", LogManager.getLogCount()},
                {"Rates", RateManager.getRateCount()},
                {"Bans", BanManager.getBanCount()},
                {"Levels", LevelManager.getLevelCount()},
                {"Wars", WarManager.getWarCount()},
                {"SubAreas", SubAreaManager.getSubAreaCount()},
        };
    }

    /**
     * Concatenates multiple row sections into a single two-column table.
     *
     * @param sections the row sections to merge
     * @return the combined table rows
     */
    public static Object[][] combine(Object[][]... sections) {
        int totalRows = Arrays.stream(sections).mapToInt(section -> section.length).sum();

        Object[][] combined = new Object[totalRows][2];
        int index = 0;

        for (Object[][] section : sections) {
            for (Object[] row : section) {
                combined[index++] = row;
            }
        }

        return combined;
    }

    private static String getLatencyStatus(long latencyMs) {
        if (latencyMs < 50) {
            return "Excellent";
        }

        if (latencyMs < 250) {
            return "Good";
        }

        if (latencyMs < 500) {
            return "Bad";
        }

        if (latencyMs < 1000) {
            return "Very Bad";
        }

        return "Unusable";
    }
}



