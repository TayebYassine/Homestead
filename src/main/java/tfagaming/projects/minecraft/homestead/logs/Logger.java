package tfagaming.projects.minecraft.homestead.logs;

import org.bukkit.Bukkit;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("Homestead");
	private static LogsFile logs;

	public Logger() {
		Logger.logs = new LogsFile();

		sendPluginBanner();
	}

	public static void info(String... message) {
		logger.info("INFO » " + String.join(" ", message));
		logs.save("[INFO] " + String.join(" ", message));
	}

	public static void warning(String... message) {
		logger.warning("WARN » " + String.join(" ", message));
		logs.save("[WARN] " + String.join(" ", message));
	}

	public static void debug(String... message) {
		if (Homestead.config.isDebugEnabled()) {
			logger.warning("DEBUG » " + String.join(" ", message));
			logs.save("[DEBUG] " + String.join(" ", message));
		}
	}

	public static void error(String message) {
		logger.severe("ERROR » " + String.join(" ", message));
		logs.save("[ERROR] " + message);
	}

	public static void error(Throwable error) {
		Logger.error("An unexpected error occurred while running Homestead. The plugin could be disabled at any time to avoid any exploits or data corruption.");
		Logger.error("Please report the issue to the GitHub issues tracker or on the Discord server to resolve it as soon as possible.");

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		error.printStackTrace(pw);
		String fullStackTrace = sw.toString();

		Logger.error(fullStackTrace);
	}

	public void sendPluginBanner() {
		StringBuilder lineSplitter = new StringBuilder();

		lineSplitter.append("-".repeat(54));

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

	public static class Colors {
		public static final String RED = "\u001B[31m";
		public static final String YELLOW = "\u001B[33m";
		public static final String GREEN = "\u001B[32m";
		public static final String BLUE = "\u001B[34m";
		public static final String CYAN = "\u001B[36m";

		public static final String _RESET = "\u001B[0m";
	}
}
