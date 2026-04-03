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

public final class Messages {
	public static void send(Player player, String... message) {
		sendFormatted(player, String.join(" ", message), null);
	}

	public static void send(Player player, String message) {
		sendFormatted(player, message, null);
	}

	public static void send(Player player, int path) {
		sendFormatted(player, path, null);
	}

	public static void send(Player player, String message, Placeholder placeholder) {
		sendFormatted(player, message, placeholder);
	}

	public static void send(Player player, int path, Placeholder placeholder) {
		sendFormatted(player, path, placeholder);
	}

	public static void send(CommandSender sender, String... message) {
		sendFormatted(sender, String.join(" ", message), null);
	}

	public static void send(CommandSender sender, String message) {
		sendFormatted(sender, message, null);
	}

	public static void send(CommandSender sender, int path) {
		sendFormatted(sender, path, null);
	}

	public static void send(CommandSender sender, String message, Placeholder placeholder) {
		sendFormatted(sender, message, placeholder);
	}

	public static void send(CommandSender sender, int path, Placeholder placeholder) {
		sendFormatted(sender, path, placeholder);
	}

	private static void sendFormatted(Object receiver, int path, Placeholder placeholder) {
		Object object = Resources.<LanguageFile>get(ResourceType.Language).getRaw(String.valueOf(path));

		if (object instanceof String string) {
			sendFormatted(receiver, string, placeholder);
		} else if (object instanceof List<?> list) {
			for (Object e : list) {
				sendFormatted(receiver, String.valueOf(e), placeholder);
			}
		}
	}

	private static void sendFormatted(Object receiver, String message, Placeholder placeholder) {
		if (placeholder == null) {
			placeholder = new Placeholder();
		}

		placeholder.add("{__prefix__}", Resources.<LanguageFile>get(ResourceType.Language).getPrefix());

		message = Formatter.applyPlaceholders(message, placeholder);

		if (receiver instanceof Player player) {
			PlatformBridge.get().sendMessage(player, message);
		} else if (receiver instanceof CommandSender sender) {
			sender.sendMessage(ColorTranslator.stripForConsole(message));
		}
	}
}