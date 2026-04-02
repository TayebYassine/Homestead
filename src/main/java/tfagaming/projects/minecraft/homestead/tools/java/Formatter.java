package tfagaming.projects.minecraft.homestead.tools.java;

import org.bukkit.*;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Formatter {
	public static String applyPlaceholders(String string, Placeholder placeholder) {
		Map<String, String> replacements = placeholder.build();

		return applyPlaceholders(string, replacements);
	}

	private static String applyPlaceholders(String string, Map<String, String> replacements) {
		if (string == null) {
			return "NULL";
		}

		for (Map.Entry<String, String> entry : replacements.entrySet()) {
			string = string.replace(entry.getKey(), entry.getValue() == null ? "NULL" : entry.getValue());
		}

		return string;
	}

	public static String getLocation(Location location) {
		World world = location.getWorld();

		return applyPlaceholders(
				Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.location"),
				new Placeholder()
						.add("{world}", world == null ? "UnknownWorld" : world.getName())
						.add("{x}", NumberUtils.truncate(location.getX()))
						.add("{y}", NumberUtils.truncate(location.getY()))
						.add("{z}", NumberUtils.truncate(location.getZ()))
		);
	}

	public static String getLocationChunk(Chunk chunk) {
		World world = chunk.getWorld();

		return applyPlaceholders(
				Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.chunk"),
				new Placeholder()
						.add("{world}", world.getName())
						.add("{x}", NumberUtils.truncate(chunk.getX()))
						.add("{z}", NumberUtils.truncate(chunk.getZ()))
		);
	}

	public static String getBalance(double amount) {
		String balance = NumberUtils.convertToBalance(amount);
		String format = Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.balance");

		return format.replace("{balance}", balance);
	}

	public static String getDate(long date) {
		String pattern = Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.date-format");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String formatted = simpleDateFormat.format(date);
		String dateWithAgo = Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.date");

		Placeholder placeholder = new Placeholder()
				.add("{date}", formatted)
				.add("{time-ago}", getAgo(date));

		return applyPlaceholders(dateWithAgo, placeholder);
	}

	public static String getRating(double rate) {
		return getRating(Math.round((float) rate));
	}

	public static String getRating(int rate) {
		if (rate == 0) {
			return getNone();
		}

		String star = Resources.<LanguageFile>get(ResourceType.Language).getString("default.star");

		return getStarColor(rate) + star.repeat(rate);
	}

	private static String getStarColor(int rate) {
		return Resources.<LanguageFile>get(ResourceType.Language).getString("default.star-color." + rate);
	}

	public static String getRemainingTime(long time) {
		long currentTime = System.currentTimeMillis();

		long differenceMillis = time - currentTime;
		long totalSeconds = differenceMillis / 1000;

		long days = totalSeconds / 86400;
		long remaining = totalSeconds % 86400;

		long hours = remaining / 3600;
		remaining = remaining % 3600;

		long minutes = remaining / 60;
		long seconds = remaining % 60;

		return applyPlaceholders(
				Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.duration"),
				new Placeholder()
						.add("{d}", days)
						.add("{h}", hours)
						.add("{m}", minutes)
						.add("{s}", seconds)
		);
	}

	public static String getPlayerPlaytime(OfflinePlayer player) {
		long totalMinutes = getPlayerMinutes(player);
		long totalSeconds = totalMinutes * 60;

		long days = totalSeconds / 86400;
		long remainingSeconds = totalSeconds % 86400;

		long hours = remainingSeconds / 3600;
		remainingSeconds %= 3600;

		long minutes = remainingSeconds / 60;
		long seconds = remainingSeconds % 60;

		return applyPlaceholders(
				Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.duration"),
				new Placeholder()
						.add("{d}", days)
						.add("{h}", hours)
						.add("{m}", minutes)
						.add("{s}", seconds)
		);
	}

	private static long getPlayerMinutes(OfflinePlayer player) {
		return player.getStatistic(Statistic.PLAY_ONE_MINUTE) / (20L * 60L);
	}

	public static String formatPaginationMenuTitle(String title, int currentPage, int totalPages) {
		return applyPlaceholders(
				Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.gui-pagination-title"),
				new Placeholder()
						.add("{title}", title)
						.add("{current-page}", currentPage)
						.add("{total-pages}", totalPages)
		);
	}

	public static String formatPrivateChat(String regionName, String sender, String message) {
		return applyPlaceholders(
				Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.private-chat"),
				new Placeholder()
						.add("{region}", regionName)
						.add("{sender}", sender)
						.add("{message}", message)
		);
	}

	public static String getPlayerOwnedRegions(OfflinePlayer player) {
		List<Region> regions = RegionManager.getRegionsOwnedByPlayer(player);

		if (regions.isEmpty()) {
			return getNone();
		}

		String format = Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.player-regions");

		return regions.stream()
				.map((region) -> format.replace("{region}", region.getName()))
				.collect(Collectors.joining(Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.player-regions-joining")));
	}

	public static String getPlayerTrustedRegions(OfflinePlayer player) {
		List<Region> regions = RegionManager.getRegionsHasPlayerAsMember(player);

		if (regions.isEmpty()) {
			return getNone();
		}

		String format = Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.player-regions");

		return regions.stream()
				.map((region) -> format.replace("{region}", region.getName()))
				.collect(Collectors.joining(Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.player-regions-joining")));
	}

	public static String getMembersOfRegion(Region region) {
		List<SerializableMember> members = region.getMembers();

		if (members.isEmpty()) {
			return getNone();
		}

		String format = Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.region-members");

		return members.stream()
				.map((member) -> format.replace("{playername}",
								member.bukkit() == null
										? "Unknown"
										: Objects.requireNonNull(member.bukkit().getName())
						)
				)
				.collect(Collectors.joining(Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.region-members-joining")));
	}

	public static String getRegionsOfWar(War war) {
		List<Region> regions = war.getRegions();

		if (regions.isEmpty()) {
			return getNone();
		}

		String format = Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.war-regions");

		return regions.stream()
				.map((region) -> format.replace("{region}", region.getName()))
				.collect(Collectors.joining(Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.war-regions-joining")));
	}

	public static String getNone() {
		return Resources.<LanguageFile>get(ResourceType.Language).getString("default.none");
	}

	public static String getNever() {
		return Resources.<LanguageFile>get(ResourceType.Language).getString("default.never");
	}

	public static String getBoolean(boolean value) {
		return Resources.<LanguageFile>get(ResourceType.Language).getString(value ? "default.isTrue" : "default.isFalse");
	}

	public static String getToggle(boolean value) {
		return Resources.<LanguageFile>get(ResourceType.Language).getString(value ? "default.isEnabled" : "default.isDisabled");
	}

	public static String getFlagState(boolean value) {
		return Resources.<LanguageFile>get(ResourceType.Language).getString(value ? "default.flagSet" : "default.flagUnset");
	}

	public static String getPlayerStatus(OfflinePlayer player) {
		return Bukkit.getBannedPlayers().contains(player) ? Resources.<LanguageFile>get(ResourceType.Language).getString("default.banned")
						: (player.isOnline() ? Resources.<LanguageFile>get(ResourceType.Language).getString("default.online")
						: Resources.<LanguageFile>get(ResourceType.Language).getString("default.offline"));
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
			return Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.ago-days").replace("{v}", String.valueOf(days));
		}

		if (hours != 0) {
			return Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.ago-hours").replace("{v}", String.valueOf(hours));
		}

		if (minutes != 0) {
			return Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.ago-minutes").replace("{v}", String.valueOf(minutes));
		}

		if (seconds != 0) {
			return Resources.<ConfigFile>get(ResourceType.Config).getString("formatters.ago-seconds").replace("{v}", String.valueOf(seconds));
		}

		return "0";
	}
}
