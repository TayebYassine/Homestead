package tfagaming.projects.minecraft.homestead.tools.minecraft.chat;

import org.bukkit.ChatColor;

public final class ColorTranslator {
	private static final String STANDARD_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";

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
							i += 8; // skip &#RRGGBB
							continue;
						}
					}

					if (i + 4 < input.length() + 1) {
						String hex3 = safeSubstring(input, i + 2, i + 5);
						if (hex3 != null && hex3.matches("[0-9A-Fa-f]{3}")) {
							String expanded = expandHex(hex3);
							out.append(net.md_5.bungee.api.ChatColor.of('#' + expanded));
							i += 5; // skip &#RGB
							continue;
						}
					}
				}

				if (STANDARD_CODES.indexOf(next) >= 0) {
					out.append(ChatColor.getByChar(Character.toLowerCase(next)));
					i += 2; // skip &<code>
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