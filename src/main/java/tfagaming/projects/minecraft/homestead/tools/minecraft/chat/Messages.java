package tfagaming.projects.minecraft.homestead.tools.minecraft.chat;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.platform.PlatformBridge;

import java.util.List;
import java.util.stream.Collectors;

public final class Messages {
	private Messages() {
	}

	// Player
	public static void send(Player player, String path) {
		sendFormattedPath(player, path);
	}

	public static void send(Player player, String path, Placeholder placeholder) {
		sendFormattedPath(player, path, placeholder);
	}

	public static void send(Player player, String path, Object... args) {
		sendFormattedPath(player, path, args);
	}

	public static void sendString(Player player, String message) {
		sendFormatted(player, message);
	}

	public static void sendString(Player player, String message, Placeholder placeholder) {
		sendFormatted(player, message, placeholder);
	}

	public static void sendString(Player player, String message, Object... args) {
		sendFormatted(player, message, args);
	}

	// Console
	public static void send(CommandSender sender, String path) {
		sendFormattedPath(sender, path);
	}


	public static void send(CommandSender sender, String path, Placeholder placeholder) {
		sendFormattedPath(sender, path, placeholder);
	}

	public static void send(CommandSender sender, String path, Object... args) {
		sendFormattedPath(sender, path, args);
	}

	public static void sendString(CommandSender sender, String message) {
		sendFormatted(sender, message);
	}

	public static void sendString(CommandSender sender, String message, Placeholder placeholder) {
		sendFormatted(sender, message, placeholder);
	}

	public static void sendString(CommandSender sender, String message, Object... args) {
		sendFormatted(sender, message, args);
	}

	// Utility
	private static void sendFormattedPath(Object receiver, String path, Placeholder placeholder) {
		Object obj = Resources.<LanguageFile>get(ResourceType.Language).getRaw(path);

		if (obj == null) obj = "NULL";

		String message = "INCOMPATIBLE-TYPE";

		if (obj instanceof String str) {
			message = str;
		} else if (obj instanceof List<?> list) {
			message = list.stream().map(String::valueOf).collect(Collectors.joining("\n"));
		}

		sendFormatted(receiver, message, placeholder);
	}

	private static void sendFormattedPath(Object receiver, String path, Object... args) {
		Object obj = Resources.<LanguageFile>get(ResourceType.Language).getRaw(path);

		if (obj == null) obj = "NULL";

		String message = "INCOMPATIBLE-TYPE";

		if (obj instanceof String str) {
			message = str;
		} else if (obj instanceof List<?> list) {
			message = list.stream().map(String::valueOf).collect(Collectors.joining("\n"));
		}

		sendFormatted(receiver, message, args);
	}

	private static void sendFormatted(Object receiver, String message, Placeholder placeholder) {

		if (placeholder == null) {
			placeholder = new Placeholder();
		}

		if (receiver instanceof Player) {
			placeholder.add("{__prefix__}", Resources.<LanguageFile>get(ResourceType.Language).getPrefix());
		} else {
			message = message.replace("{__prefix__}", "").trim();
		}

		message = Formatter.applyPlaceholders(message, placeholder).trim();

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

		message = Formatter.applyPlaceholders(message, placeholder).trim();

		if (receiver instanceof Player player) {
			PlatformBridge.get().sendMessage(player, message);
		} else if (receiver instanceof CommandSender sender) {
			sender.sendMessage(ColorTranslator.stripForConsole(message));
		}
	}
}