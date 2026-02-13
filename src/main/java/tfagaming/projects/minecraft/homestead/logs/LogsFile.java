package tfagaming.projects.minecraft.homestead.logs;

import tfagaming.projects.minecraft.homestead.Homestead;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class LogsFile {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy");
	private final File logFile;
	private boolean isReady = false;

	public LogsFile() {
		this.logFile = new File(Homestead.getInstance().getDataFolder(), "logs.txt");
		createLogFile();

		clear();

		save("-------------------------------------------------------------------");
		save("This is the logs.txt file; it saves all command executions and errors from the plugin.");
		save("Do not delete this file while the plugin is running.");
		save("-------------------------------------------------------------------");
	}

	private void createLogFile() {
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException ignored) {

			}
		}

		isReady = true;
	}

	public void save(String message) {
		if (!isReady) {
			return;
		}

		try (FileWriter writer = new FileWriter(logFile, true)) {
			String timestamp = LocalDateTime.now().format(formatter);
			writer.write("[" + timestamp + "] " + message + "\n");
		} catch (IOException e) {

		}
	}

	public void clear() {
		if (!isReady) {
			return;
		}

		try (FileWriter writer = new FileWriter(logFile, false)) {
			writer.write("");
		} catch (IOException e) {

		}
	}
}
