package me.tayebyassine.homestead.logs;

import me.tayebyassine.homestead.Homestead;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages the plugin's persistent log file ({@code logs.txt}).
 *
 * <p>Each entry is prefixed with a timestamp in {@code HH:mm:ss MM/dd/yyyy}
 * format. The file is cleared and re-initialized on every server start.</p>
 */
public final class LogsFile {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy");
    private final File logFile;
    private boolean isReady = false;

    /**
     * Create or open the log file, clear its contents, and write the
     * header banner.
     */
    public LogsFile() {
        this.logFile = new File(Homestead.getInstance().getDataFolder(), "logs.txt");
        createLogFile();

        clear();

        save("-------------------------------------------------------------------");
        save("This is the logs.txt file; it saves all command executions and errors from the plugin.");
        save("Do not delete this file while the plugin is running.");
        save("-------------------------------------------------------------------");
    }

    /**
     * Ensure the log file exists on disk. Sets {@link #isReady} to
     * {@code true} on success.
     */
    private void createLogFile() {
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException ignored) {

            }
        }

        isReady = true;
    }

    /**
     * Append a timestamped message to the log file.
     *
     * @param message the message to write
     */
    public void save(String message) {
        if (!isReady) {
            return;
        }

        try (FileWriter writer = new FileWriter(logFile, true)) {
            String timestamp = LocalDateTime.now().format(formatter);
            writer.write("[" + timestamp + "] " + message + "\n");
        } catch (IOException ignored) {

        }
    }

    /**
     * Clear all contents of the log file.
     */
    public void clear() {
        if (!isReady) {
            return;
        }

        try (FileWriter writer = new FileWriter(logFile, false)) {
            writer.write("");
        } catch (IOException ignored) {

        }
    }
}
