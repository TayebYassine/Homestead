package me.tayebyassine.homestead;

import me.tayebyassine.homestead.logs.Logger;
import org.bukkit.Bukkit;

public final class ProtectionMode {
	private static volatile boolean ENABLED = false;
	private static volatile boolean MANUALLY_DISABLED = false;

	private ProtectionMode() {
	}

	public static boolean isEnabled() {
		return ENABLED;
	}

	public static void enableAutomatic() {
		if (MANUALLY_DISABLED) {
			return;
		}

		enable();
	}

	public static void enable() {
		if (ENABLED) {
			return;
		}

		ENABLED = true;
		MANUALLY_DISABLED = false;

		Logger.error("============================================================");
		Logger.error("PROTECTION MODE HAS BEEN ENABLED");
		Logger.error("The plugin encountered a critical failure and can no longer");
		Logger.error("operate normally. Every region claim is now locked down and");
		Logger.error("no player can modify claimed chunks, including server OPs.");
		Logger.error("Use '/hsadmin protectionmode false' to disable it manually once");
		Logger.error("the underlying issue is resolved.");
		Logger.error("============================================================");

		Bukkit.broadcast("§c[HOMESTEAD] Protection mode has been enabled. All region claims are locked down.", "homestead.commands.homesteadadmin");
		Bukkit.broadcast("§c[HOMESTEAD] The plugin is not fully functional. An operator must run §7/hsadmin protectionmode false§c once the issue is fixed.", "homestead.commands.homesteadadmin");
		Bukkit.broadcast("§c[HOMESTEAD] Please check the console for more details.", "homestead.commands.homesteadadmin");
	}

	public static void disable() {
		if (!ENABLED) {
			MANUALLY_DISABLED = true;
			return;
		}

		ENABLED = false;
		MANUALLY_DISABLED = true;

		Logger.info("Protection mode disabled. The plugin has resumed normal operation.");

		Bukkit.broadcast("§a[HOMESTEAD] Protection mode has been disabled. Region claims are no longer locked down.", "homestead.commands.homesteadadmin");
	}

	public static void clearManualOverride() {
		MANUALLY_DISABLED = false;
	}

	public static void setEnabled(boolean value) {
		if (value) {
			enable();
		} else {
			disable();
		}
	}
}
