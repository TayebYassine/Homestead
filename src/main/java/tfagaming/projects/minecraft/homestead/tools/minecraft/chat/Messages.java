package tfagaming.projects.minecraft.homestead.tools.minecraft.chat;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.platform.PlatformBridge;

public final class Messages {
	private Messages() {
	}

	// Player
	public static void send(Player player, String message) {
		sendFormatted(player, message);
	}

	public static void send(Player player, String message, Placeholder placeholder) {
		sendFormatted(player, message, placeholder);
	}

	public static void send(Player player, String message, Object... args) {
		sendFormatted(player, message, args);
	}

	// Console
	public static void send(CommandSender sender, String message) {
		sendFormatted(sender, message);
	}


	public static void send(CommandSender sender, String message, Placeholder placeholder) {
		sendFormatted(sender, message, placeholder);
	}

	public static void send(CommandSender sender, String message, Object... args) {
		sendFormatted(sender, message, args);
	}

	// Utility
	private static void sendFormatted(Object receiver, String message, Placeholder placeholder) {
		if (placeholder == null) {
			placeholder = new Placeholder();
		}

		if (receiver instanceof Player) {
			placeholder.add("{__prefix__}", Resources.<LanguageFile>get(ResourceType.Language).getPrefix());
		} else {
			message = message.replace("{__prefix__}", "").trim();
		}

		message = Formatter.applyPlaceholders(message, placeholder);

		if (receiver instanceof Player player) {
			PlatformBridge.get().sendMessage(player, message);
		} else if (receiver instanceof CommandSender sender) {
			sender.sendMessage(ColorTranslator.stripForConsole(message));
		}
	}

	private static void sendFormatted(Object receiver, String message, Object... args) {
		Placeholder placeholder = new Placeholder();

		for (int i = 0; i < args.length; i++) {
			placeholder.add("{" + i + "}", args[i]);
		}

		if (receiver instanceof Player) {
			placeholder.add("{__prefix__}", Resources.<LanguageFile>get(ResourceType.Language).getPrefix());
		} else {
			message = message.replace("{__prefix__}", "").trim();
		}

		message = Formatter.applyPlaceholders(message, placeholder);

		if (receiver instanceof Player player) {
			PlatformBridge.get().sendMessage(player, message);
		} else if (receiver instanceof CommandSender sender) {
			sender.sendMessage(ColorTranslator.stripForConsole(message));
		}
	}
}