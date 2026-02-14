package tfagaming.projects.minecraft.homestead.tools.minecraft.chat;

import org.bukkit.ChatColor;

public final class ColorTranslator {
	static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

	public static String translate(String input) {
		if (input == null || input.isEmpty()) return input;

		String[] parts = input.split(String.format(WITH_DELIMITER, "&"));
		StringBuilder out = new StringBuilder();

		for (int i = 0; i < parts.length; i++) {
			if (parts[i].equals("&") && i + 1 < parts.length) {
				String code = parts[++i];

				if (code.startsWith("#")) {
					String rawHex = code.substring(1);
					String fullHex = expandHex(rawHex);
					if (fullHex != null) {
						int len = 1 + fullHex.length();
						out.append(net.md_5.bungee.api.ChatColor.of('#' + fullHex));

						if (code.length() > len) {
							out.append(ChatColor.translateAlternateColorCodes('&',
									"&" + code.substring(len)));
						}
						continue;
					}
				}

				out.append(ChatColor.translateAlternateColorCodes('&', "&" + code));
			} else {
				out.append(parts[i]);
			}
		}
		return out.toString();
	}

	public static String preserve(String string) {
		if (string == null) {
			return null;
		}

		string = string.replaceAll("&[a-fA-F0-9k-orK-OR]", "");
		string = string.replaceAll("§[a-fA-F0-9k-orK-OR]", "");

		return string;
	}

	private static String expandHex(String hex) {
		if (hex.length() == 6 && hex.matches("[0-9A-Fa-f]{6}")) return hex;
		if (hex.length() == 3 && hex.matches("[0-9A-Fa-f]{3}")) {
			return "" + hex.charAt(0) + hex.charAt(0) +
					hex.charAt(1) + hex.charAt(1) +
					hex.charAt(2) + hex.charAt(2);
		}
		return null;
	}
}
