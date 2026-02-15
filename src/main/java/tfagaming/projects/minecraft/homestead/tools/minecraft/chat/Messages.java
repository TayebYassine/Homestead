package tfagaming.projects.minecraft.homestead.tools.minecraft.chat;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;

public final class Messages {
	public static void send(Player player, String... message) {
		sendFormatted(player, String.join(" ", message), null);
	}

	public static void send(Player player, String message) {
		sendFormatted(player, message, null);
	}

	public static void send(Player player, int path) {
		sendFormatted(player, Homestead.language.getString(String.valueOf(path)), null);
	}

	public static void send(Player player, String message, Placeholder placeholder) {
		sendFormatted(player, message, placeholder);
	}

	public static void send(Player player, int path, Placeholder placeholder) {
		sendFormatted(player, Homestead.language.getString(String.valueOf(path)), placeholder);
	}

	public static void send(CommandSender sender, String... message) {
		sendFormatted(sender, String.join(" ", message), null);
	}

	public static void send(CommandSender sender, String message) {
		sendFormatted(sender, message, null);
	}

	public static void send(CommandSender sender, int path) {
		sendFormatted(sender, Homestead.language.getString(String.valueOf(path)), null);
	}

	public static void send(CommandSender sender, String message, Placeholder placeholder) {
		sendFormatted(sender, message, placeholder);
	}

	public static void send(CommandSender sender, int path, Placeholder placeholder) {
		sendFormatted(sender, Homestead.language.getString(String.valueOf(path)), placeholder);
	}

	private static void sendFormatted(Object receiver, String message, Placeholder placeholder) {
		if (placeholder != null) {
			placeholder.add("{__prefix__}", Homestead.config.getPrefix());

			message = Formatters.applyPlaceholders(message, placeholder.build());
		}

		if (receiver instanceof Player player) {
			player.sendMessage(ColorTranslator.translate(message));
		} else if (receiver instanceof CommandSender sender) {
			sender.sendMessage(ColorTranslator.preserve(message));
		}
	}
}