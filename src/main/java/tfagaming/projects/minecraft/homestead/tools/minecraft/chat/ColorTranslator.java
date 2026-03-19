package tfagaming.projects.minecraft.homestead.tools.minecraft.chat;

import org.bukkit.ChatColor;
import java.util.Map;

public final class ColorTranslator {
	private static final String STANDARD_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";

	private static final Map<Character, String> LEGACY_TO_MM_TAG = Map.ofEntries(
			Map.entry('0', "black"),
			Map.entry('1', "dark_blue"),
			Map.entry('2', "dark_green"),
			Map.entry('3', "dark_aqua"),
			Map.entry('4', "dark_red"),
			Map.entry('5', "dark_purple"),
			Map.entry('6', "gold"),
			Map.entry('7', "gray"),
			Map.entry('8', "dark_gray"),
			Map.entry('9', "blue"),
			Map.entry('a', "green"),
			Map.entry('b', "aqua"),
			Map.entry('c', "red"),
			Map.entry('d', "light_purple"),
			Map.entry('e', "yellow"),
			Map.entry('f', "white"),
			Map.entry('k', "obfuscated"),
			Map.entry('l', "bold"),
			Map.entry('m', "strikethrough"),
			Map.entry('n', "underlined"),
			Map.entry('o', "italic"),
			Map.entry('r', "reset")
	);

	public static String legacyToMiniMessage(String input) {
		if (input == null || input.isEmpty()) return input;

		StringBuilder out = new StringBuilder();
		int i = 0;

		while (i < input.length()) {
			char c = input.charAt(i);

			// Skip over existing MiniMessage tags untouched
			if (c == '<') {
				int close = input.indexOf('>', i);
				if (close != -1) {
					out.append(input, i, close + 1);
					i = close + 1;
					continue;
				}
			}

			if (c == '&' && i + 1 < input.length()) {
				char next = input.charAt(i + 1);

				// &#RRGGBB hex
				if (next == '#') {
					String hex6 = safeSubstring(input, i + 2, i + 8);
					if (hex6 != null && hex6.matches("[0-9A-Fa-f]{6}")) {
						out.append("<color:#").append(hex6).append('>');
						i += 8;
						continue;
					}

					// &#RGB
					String hex3 = safeSubstring(input, i + 2, i + 5);
					if (hex3 != null && hex3.matches("[0-9A-Fa-f]{3}")) {
						out.append("<color:#").append(expandHex(hex3)).append('>');
						i += 5;
						continue;
					}
				}

				// Standard &X code
				String tag = LEGACY_TO_MM_TAG.get(Character.toLowerCase(next));
				if (tag != null) {
					out.append('<').append(tag).append('>');
					i += 2;
					continue;
				}
			}

			out.append(c);
			i++;
		}

		return out.toString();
	}

	public static String translate(String input) {
		if (input == null || input.isEmpty()) return input;

		StringBuilder out = new StringBuilder();
		int i = 0;

		while (i < input.length()) {
			char c = input.charAt(i);

			if (c == '&' && i + 1 < input.length()) {
				char next = input.charAt(i + 1);

				if (next == '#') {
					if (i + 7 < input.length() + 1) {
						String hex6 = safeSubstring(input, i + 2, i + 8);
						if (hex6 != null && hex6.matches("[0-9A-Fa-f]{6}")) {
							out.append(net.md_5.bungee.api.ChatColor.of('#' + hex6));
							i += 8;
							continue;
						}
					}

					if (i + 4 < input.length() + 1) {
						String hex3 = safeSubstring(input, i + 2, i + 5);
						if (hex3 != null && hex3.matches("[0-9A-Fa-f]{3}")) {
							out.append(net.md_5.bungee.api.ChatColor.of('#' + expandHex(hex3)));
							i += 5;
							continue;
						}
					}
				}

				if (STANDARD_CODES.indexOf(next) >= 0) {
					out.append(ChatColor.getByChar(Character.toLowerCase(next)));
					i += 2;
					continue;
				}
			}

			out.append(c);
			i++;
		}

		return out.toString();
	}

	public static String preserve(String string) {
		if (string == null) return null;

		string = string.replaceAll("&?[&§]#[0-9A-Fa-f]{6}", "");
		string = string.replaceAll("&?[&§]#[0-9A-Fa-f]{3}", "");
		string = string.replaceAll("[&§][0-9A-Fa-f k-orK-OR]", "");

		return string;
	}

	public static String stripForConsole(String input) {
		if (input == null) return null;
		input = input.replaceAll("<[^>]+>", "");
		return preserve(input);
	}

	private static String expandHex(String hex3) {
		return "" + hex3.charAt(0) + hex3.charAt(0)
				+ hex3.charAt(1) + hex3.charAt(1)
				+ hex3.charAt(2) + hex3.charAt(2);
	}

	private static String safeSubstring(String s, int start, int end) {
		if (start < 0 || end > s.length()) return null;
		return s.substring(start, end);
	}
}