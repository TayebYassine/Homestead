package tfagaming.projects.minecraft.homestead.tools.java;

import org.bukkit.*;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Formatters {
	public static String applyPlaceholders(String string, Placeholder placeholder) {
		Map<String, String> replacements = placeholder.build();

		return applyPlaceholders(string, replacements);
	}

	public static String applyPlaceholders(String string, Map<String, String> replacements) {
		if (string == null) {
			return "NULL";
		}

		for (Map.Entry<String, String> entry : replacements.entrySet()) {
			string = string.replace(entry.getKey(), entry.getValue() == null ? "NULL" : entry.getValue());
		}

		return string;
	}

	public static String formatLocation(Location location) {
		HashMap<String, String> replacements = new HashMap<String, String>();

		replacements.put("{world}", location.getWorld().getName());
		replacements.put("{x}", String.valueOf(NumberUtils.truncateToTwoDecimalPlaces(location.getX())));
		replacements.put("{y}", String.valueOf(NumberUtils.truncateToTwoDecimalPlaces(location.getY())));
		replacements.put("{z}", String.valueOf(NumberUtils.truncateToTwoDecimalPlaces(location.getZ())));

		return ColorTranslator.translate(applyPlaceholders(Homestead.config.getString("formatters.location"), replacements));
	}

	public static String formatChunk(Chunk chunk) {
		HashMap<String, String> replacements = new HashMap<String, String>();

		replacements.put("{world}", chunk.getWorld().getName());
		replacements.put("{x}", String.valueOf(NumberUtils.truncateToTwoDecimalPlaces(chunk.getX())));
		replacements.put("{z}", String.valueOf(NumberUtils.truncateToTwoDecimalPlaces(chunk.getZ())));

		return ColorTranslator.translate(applyPlaceholders(Homestead.config.getString("formatters.location"), replacements));
	}

	public static String getBalance(double amount) {
		String balance = NumberUtils.convertToBalance(amount);
		String format = Homestead.config.getString("formatters.balance");

		return format.replace("{balance}", balance);
	}

	public static String getDate(long date) {
		String pattern = Homestead.config.getString("formatters.date-format");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String formatted = simpleDateFormat.format(date);
		String dateWithAgo = Homestead.config.getString("formatters.date");

		Map<String, String> replacements = new HashMap<String, String>();

		replacements.put("{date}", formatted);
		replacements.put("{time-ago}", getAgo(date));

		return applyPlaceholders(dateWithAgo, replacements);
	}

	public static String formatRating(double rate) {
		return formatRating(Math.round((float) rate));
	}

	public static String formatRating(int rate) {
		String star = Homestead.language.getString("default.star");

		switch (rate) {
			case 1:
				return ColorTranslator.translate("&c" + star.repeat(1));
			case 2:
				return ColorTranslator.translate("&6" + star.repeat(2));
			case 3:
				return ColorTranslator.translate("&e" + star.repeat(3));
			case 4:
				return ColorTranslator.translate("&a" + star.repeat(4));
			case 5:
				return ColorTranslator.translate("&a" + star.repeat(5));
			default:
				return getNone();
		}
	}

	public static String formatRemainingTime(long time) {
		long currentTime = System.currentTimeMillis();

		long differenceMillis = time - currentTime;
		long totalSeconds = differenceMillis / 1000;

		long days = totalSeconds / 86400;
		long remaining = totalSeconds % 86400;

		long hours = remaining / 3600;
		remaining = remaining % 3600;

		long minutes = remaining / 60;
		long seconds = remaining % 60;

		HashMap<String, String> replacements = new HashMap<String, String>();

		replacements.put("{d}", String.valueOf(days));
		replacements.put("{h}", String.valueOf(hours));
		replacements.put("{m}", String.valueOf(minutes));
		replacements.put("{s}", String.valueOf(seconds));

		return ColorTranslator
				.translate(applyPlaceholders(Homestead.config.getString("formatters.duration"), replacements));
	}

	public static String formatPlayerPlaytimeDuration(OfflinePlayer player) {
		long totalMinutes = getPlayerMinutes(player);
		long totalSeconds = totalMinutes * 60;

		long days = totalSeconds / 86400;
		long remainingSeconds = totalSeconds % 86400;

		long hours = remainingSeconds / 3600;
		remainingSeconds %= 3600;

		long minutes = remainingSeconds / 60;
		long seconds = remainingSeconds % 60;

		HashMap<String, String> replacements = new HashMap<String, String>();

		replacements.put("{d}", String.valueOf(days));
		replacements.put("{h}", String.valueOf(hours));
		replacements.put("{m}", String.valueOf(minutes));
		replacements.put("{s}", String.valueOf(seconds));

		return ColorTranslator
				.translate(applyPlaceholders(Homestead.config.getString("formatters.duration"), replacements));
	}

	private static long getPlayerMinutes(OfflinePlayer player) {
		return player.getStatistic(Statistic.PLAY_ONE_MINUTE) / (20L * 60L);
	}

	public static String formatPaginationMenuTitle(String title, int currentPage, int totalPages) {
		HashMap<String, String> replacements = new HashMap<String, String>();

		replacements.put("{title}", title);
		replacements.put("{current-page}", String.valueOf(currentPage));
		replacements.put("{total-pages}", String.valueOf(totalPages));

		return ColorTranslator
				.translate(applyPlaceholders(Homestead.config.getString("formatters.gui-pagination-title"), replacements));
	}

	public static String formatPrivateChat(String regionName, String sender, String message) {
		HashMap<String, String> replacements = new HashMap<String, String>();

		replacements.put("{region}", regionName);
		replacements.put("{sender}", sender);
		replacements.put("{message}", message);

		return ColorTranslator
				.translate(applyPlaceholders(Homestead.config.getString("formatters.private-chat"), replacements));
	}

	public static String getPlayerOwnedRegions(OfflinePlayer player) {
		List<Region> regions = RegionsManager.getRegionsOwnedByPlayer(player);

		if (regions.isEmpty()) {
			return getNone();
		}

		String format = Homestead.config.getString("formatters.player-regions");

		return ColorTranslator.translate(regions.stream()
				.map((region) -> format.replace("{region}", region.getName()))
				.collect(Collectors.joining(Homestead.config.getString("formatters.player-regions-joining"))));
	}

	public static String getPlayerTrustedRegions(OfflinePlayer player) {
		List<Region> regions = RegionsManager.getRegionsHasPlayerAsMember(player);

		if (regions.isEmpty()) {
			return getNone();
		}

		String format = Homestead.config.getString("formatters.player-regions");

		return ColorTranslator.translate(regions.stream()
				.map((region) -> format.replace("{region}", region.getName()))
				.collect(Collectors.joining(Homestead.config.getString("formatters.player-regions-joining"))));
	}

	public static String getMembersOfRegion(Region region) {
		List<SerializableMember> members = region.getMembers();

		if (members.isEmpty()) {
			return getNone();
		}

		String format = Homestead.config.getString("formatters.region-members");

		return ColorTranslator.translate(members.stream()
				.map((member) -> format.replace("{playername}", member.getBukkitOfflinePlayer().getName()))
				.collect(Collectors.joining(Homestead.config.getString("formatters.region-members-joining"))));
	}

	public static String getRegionsOfWar(War war) {
		List<Region> regions = war.getRegions();

		if (regions.isEmpty()) {
			return getNone();
		}

		String format = Homestead.config.getString("formatters.war-regions");

		return ColorTranslator.translate(regions.stream()
				.map((region) -> format.replace("{region}", region.getName()))
				.collect(Collectors.joining(Homestead.config.getString("formatters.war-regions-joining"))));
	}

	public static String getNone() {
		return ColorTranslator.translate(Homestead.language.getString("default.none"));
	}

	public static String getBoolean(boolean value) {
		return ColorTranslator.translate(Homestead.language.getString(value ? "default.isTrue" : "default.isFalse"));
	}

	public static String getEnabled(boolean value) {
		return ColorTranslator
				.translate(Homestead.language.getString(value ? "default.isEnabled" : "default.isDisabled"));
	}

	public static String getFlag(boolean value) {
		return ColorTranslator.translate(Homestead.language.getString(value ? "default.flagSet" : "default.flagUnset"));
	}

	public static String getPlayerStatus(OfflinePlayer player) {
		return ColorTranslator
				.translate(Bukkit.getBannedPlayers().contains(player) ? Homestead.language.getString("default.banned")
						: (player.isOnline() ? Homestead.language.getString("default.online")
						: Homestead.language.getString("default.offline")));
	}

	public static String getAgo(long time) {
		long currentTime = System.currentTimeMillis();

		long differenceMillis = currentTime - time;
		long totalSeconds = differenceMillis / 1000;

		long days = totalSeconds / 86400;
		long remaining = totalSeconds % 86400;

		long hours = remaining / 3600;
		remaining = remaining % 3600;

		long minutes = remaining / 60;
		long seconds = remaining % 60;

		if (days != 0) {
			return Homestead.config.getString("formatters.ago-days").replace("{v}", String.valueOf(days));
		}

		if (hours != 0) {
			return Homestead.config.getString("formatters.ago-hours").replace("{v}", String.valueOf(hours));
		}

		if (minutes != 0) {
			return Homestead.config.getString("formatters.ago-minutes").replace("{v}", String.valueOf(minutes));
		}

		if (seconds != 0) {
			return Homestead.config.getString("formatters.ago-seconds").replace("{v}", String.valueOf(seconds));
		}

		return "0";
	}
}
