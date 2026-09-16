package me.tayebyassine.homestead.logs;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import org.bukkit.Bukkit;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Central logging facade for the plugin.
 *
 * <p>Routes messages to both the server logger and the persistent
 * {@link LogsFile}. Debug output is only emitted when debug mode is
 * enabled in the configuration.</p>
 */
public class Logger {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("Homestead");
    private static LogsFile logs;

    /**
     * Initialize the logger and display the plugin banner.
     */
    public Logger() {
        Logger.logs = new LogsFile();

        sendPluginBanner();
    }

    /**
     * Log a predefined info message.
     *
     * @param message the predefined message
     */
    public static void info(PredefinedMessage message) {
        for (String each : message.getStrings()) {
            info(each);
        }
    }

    /**
     * Log an info-level message.
     *
     * @param message the message parts to join
     */
    public static void info(String... message) {
        logger.info(String.join(" ", message));
        saveLog("[INFO] " + String.join(" ", message));
    }

    /**
     * Log a predefined warning message.
     *
     * @param message the predefined message
     */
    public static void warning(PredefinedMessage message) {
        for (String each : message.getStrings()) {
            warning(each);
        }
    }

    /**
     * Log a warning-level message.
     *
     * @param message the message parts to join
     */
    public static void warning(String... message) {
        logger.warning(String.join(" ", message));
        saveLog("[WARN] " + String.join(" ", message));
    }

    /**
     * Check whether debug mode is currently enabled.
     *
     * @return {@code true} if debug mode is on
     */
    private static boolean isDebugEnabled() {
        try {
            ConfigFile config = Resources.get(ResourceType.Config);
            return config != null && config.isDebugEnabled();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Append a line to the persistent log file.
     *
     * @param line the line to write
     */
    private static void saveLog(String line) {
        if (logs != null) {
            logs.save(line);
        }
    }

    /**
     * Log a predefined debug message.
     *
     * @param message the predefined message
     */
    public static void debug(PredefinedMessage message) {
        for (String each : message.getStrings()) {
            debug(each);
        }
    }

    /**
     * Log a debug-level message. Only emitted when debug mode is enabled.
     *
     * @param message the message parts to join
     */
    public static void debug(String... message) {
        if (isDebugEnabled()) {
            logger.warning("[DEBUG-MODE] " + String.join(" ", message));
            saveLog("[DEBUG] " + String.join(" ", message));
        }
    }

    /**
     * Log a debug-level message composed of arbitrary objects. Only
     * emitted when debug mode is enabled.
     *
     * @param message the objects to join
     */
    public static void debug(Object... message) {
        if (isDebugEnabled()) {
            StringBuilder messageStr = new StringBuilder();

            for (Object each : message) {
                messageStr.append(each).append(" ");
            }

            logger.warning("[DEBUG-MODE] " + messageStr);
            logs.save("[DEBUG] " + messageStr);
        }
    }

    /**
     * Log a predefined error message.
     *
     * @param message the predefined message
     */
    public static void error(PredefinedMessage message) {
        for (String each : message.getStrings()) {
            error(each);
        }
    }

    /**
     * Log an error-level message.
     *
     * @param message the message parts to join
     */
    public static void error(String... message) {
        logger.severe(String.join(" ", message));
        saveLog("[ERROR] " + String.join(" ", message));
    }

    /**
     * Log an error-level message and print the full stack trace. Includes
     * a user-facing notice to report the issue.
     *
     * @param error the throwable to log
     */
    public static void error(Throwable error) {
        Logger.error("An unexpected error occurred while running Homestead. The plugin could be disabled at any time to avoid any exploits or data corruption.");
        Logger.error("Please report the issue to the GitHub issues tracker or on the Discord server to resolve it as soon as possible.");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        String fullStackTrace = sw.toString();

        Logger.error(fullStackTrace);
    }

    /**
     * Display the ASCII-art plugin banner and version information in the
     * server console. Also emits a snapshot warning if applicable.
     */
    public void sendPluginBanner() {
        StringBuilder lineSplitter = new StringBuilder();

        lineSplitter.repeat("-", 54);

        String banner = " _   _                           _                 _ \r\n" +
                "| | | | ___  _ __ ___   ___  ___| |_ ___  __ _  __| |\r\n" +
                "| |_| |/ _ \\| '_ ` _ \\ / _ \\/ __| __/ _ \\/ _` |/ _` |\r\n" +
                "|  _  | (_) | | | | | |  __/\\__ \\ ||  __/ (_| | (_| |\r\n" +
                "|_| |_|\\___/|_| |_| |_|\\___||___/\\__\\___|\\__,_|\\__,_|" +
                "\n\nVersion: " + Homestead.getVersion() + "\nRunning on " + Bukkit.getName() + ": "
                + Bukkit.getVersion();

        logger.info(lineSplitter.toString());

        for (String line : banner.split("\n")) {
            logger.info(Colors.CYAN + line + Colors._RESET);
        }

        logger.info(lineSplitter.toString());

        if (Homestead.isSnapshot()) {
            Logger.warning("This Homestead version is a snapshot! Bugs and exploits may be present in this JAR file.");
            Logger.warning("If you're not a developer or a contributor of Homestead, we recommend you use this JAR file for development, not for production!");
        }
    }

    /**
     * Pre-defined log messages for common plugin events.
     */
    public enum PredefinedMessage {

        /**
         * WorldGuard plugin not found.
         */
        WORLDGUARD_PLUGIN_NOT_FOUND(new String[]{
                "Unable to find the plugin 'WorldGuard' or execute API methods for its class.",
                "Please install the plugin, or disable any feature that requires the API of that extension."
        }),

        /**
         * Economy integration disabled.
         */
        ECONOMY_INTEGRATION_DISABLED(new String[]{
                "Unable to find an economy integration or execute API methods for its class.",
                "Please install a plugin that includes Economy API, or disable any feature that requires the API of that extension."
        }),

        /**
         * Update available notification.
         */
        UPDATE_FOUND(new String[]{
                "There is an available update for Homestead.",
                "Download links:",
                "> https://www.spigotmc.org/resources/121873/, ",
                "> https://modrinth.com/plugin/homestead-plugin, ",
                "> https://hangar.papermc.io/TayebYassine/Homestead"
        }),

        /**
         * Already on the latest version.
         */
        UPDATE_LATEST(new String[]{
                "You are currently on the latest version!"
        }),

        /**
         * Failed to fetch update information.
         */
        UPDATE_FETCH_FAILURE(new String[]{
                "Failed to fetch for updates, maybe GitHub is down or you are not connected to the internet.",
                "You can manually look for updates on SpigotMC, Modrinth, or Hangar!"
        });

        private final String[] strings;

        PredefinedMessage(String[] strings) {
            this.strings = strings;
        }

        PredefinedMessage(String string) {
            this.strings = new String[]{string};
        }

        /**
         * Get the message lines.
         *
         * @return the message strings
         */
        public String[] getStrings() {
            return strings;
        }
    }

    /**
     * ANSI color codes for console output.
     */
    public static class Colors {

        /**
         * Red foreground.
         */
        public static final String RED = "\u001B[31m";

        /**
         * Yellow foreground.
         */
        public static final String YELLOW = "\u001B[33m";

        /**
         * Green foreground.
         */
        public static final String GREEN = "\u001B[32m";

        /**
         * Blue foreground.
         */
        public static final String BLUE = "\u001B[34m";

        /**
         * Cyan foreground.
         */
        public static final String CYAN = "\u001B[36m";

        /**
         * Reset formatting.
         */
        public static final String _RESET = "\u001B[0m";
    }
}
