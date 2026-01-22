package tfagaming.projects.minecraft.homestead.tools.minecraft.players;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.managers.WarsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerLimits.LimitMethod;

import java.util.*;

public class PlayerUtils {
	private static final int MESSAGE_COOLDOWN_SECONDS = 3;
	private static final HashSet<UUID> COOLDOWN = new HashSet<UUID>();
	private static final Set<Long> RENT_BLACKLIST = Set.of(
			PlayerFlags.PVP,
			PlayerFlags.PASSTHROUGH
	);

	public static double getBalance(OfflinePlayer player) {
		if (!Homestead.vault.isEconomyReady()) {
			return 0.0;
		}

		return Homestead.vault.getEconomy().getBalance(player);
	}

	public static void addBalance(OfflinePlayer player, double amount) {
		if (!Homestead.vault.isEconomyReady()) {
			return;
		}

		Homestead.vault.getEconomy().depositPlayer(player, amount);
	}

	public static void removeBalance(OfflinePlayer player, double amount) {
		if (!Homestead.vault.isEconomyReady()) {
			return;
		}

		Homestead.vault.getEconomy().withdrawPlayer(player, amount);
	}

	public static void sendMessage(Player player, String... messages) {
		player.sendMessage(ChatColorTranslator.translate(String.join("", messages)));
	}

	public static void sendMessage(CommandSender sender, String path, Map<String, String> replacements) {
		String message = Homestead.language.get(path);

		if (message == null) {
			sender.sendMessage("STRING MISS AT " + path);
			return;
		}

		if (sender instanceof Player) {
			replacements.put("{__prefix__}", Homestead.config.getPrefix());

			message = Formatters.replace(message, replacements);

			sender.sendMessage(ChatColorTranslator.translate(message));
		} else {
			replacements.put("{__prefix__}", Homestead.config.getPrefix());

			message = Formatters.replace(message, replacements);

			sender.sendMessage(ChatColorTranslator.removeColor(message, true));
		}
	}

	public static void sendMessage(CommandSender sender, String path) {
		sendMessage(sender, path, new HashMap<>());
	}

	public static void sendMessage(Player player, int path, Map<String, String> replacements) {
		sendMessage(player, String.valueOf(path), replacements);
	}

	public static void sendMessage(CommandSender sender, int path, Map<String, String> replacements) {
		sendMessage(sender, String.valueOf(path), replacements);
	}

	public static void sendMessage(CommandSender sender, int path) {
		sendMessage(sender, String.valueOf(path));
	}

	public static void sendMessage(Player player, int path) {
		sendMessage(player, String.valueOf(path));
	}

	public static void sendMessageRegionEnter(Player player, Map<String, String> replacements) {
		switch (((String) Homestead.config.get("enter-exit-region-message.type")).toLowerCase()) {
			case "title":
				List<String> titleData = Homestead.config.get("enter-exit-region-message.messages.enter.title");

				player.sendTitle(ChatColorTranslator.translate(Formatters.replace(titleData.get(0), replacements)),
						ChatColorTranslator.translate(Formatters.replace(titleData.get(1), replacements)), 10, 70,
						20);

				break;
			case "actionbar":
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
						new TextComponent(ChatColorTranslator
								.translate(Formatters.replace(
										Homestead.config.get("enter-exit-region-message.messages.enter.actionbar"),
										replacements))));

				break;
			default:
				player.sendMessage(ChatColorTranslator.translate(Formatters.replace(
						Homestead.config.get("enter-exit-region-message.messages.enter.chat"),
						replacements)));
				break;
		}
	}

	public static void sendMessageRegionExit(Player player, Map<String, String> replacements) {
		switch (((String) Homestead.config.get("enter-exit-region-message.type")).toLowerCase()) {
			case "title":
				List<String> titleData = Homestead.config.get("enter-exit-region-message.messages.exit.title");

				player.sendTitle(ChatColorTranslator.translate(Formatters.replace(titleData.get(0), replacements)),
						ChatColorTranslator.translate(Formatters.replace(titleData.get(1), replacements)), 10, 70,
						20);

				break;
			case "actionbar":
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
						new TextComponent(ChatColorTranslator
								.translate(Formatters.replace(
										Homestead.config.get("enter-exit-region-message.messages.exit.actionbar"),
										replacements))));

				break;
			default:
				player.sendMessage(ChatColorTranslator.translate(Formatters.replace(
						Homestead.config.get("enter-exit-region-message.messages.exit.chat"),
						replacements)));
				break;
		}
	}

	public static void teleportPlayerToChunk(Player player, Chunk chunk) {
		Location location = ChunksManager.getLocation(player, chunk);

		player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
	}

	public static boolean isOperator(Player player) {
		if (player.isOp()) {
			return true;
		} else return player.hasPermission("homestead.operator");
	}

	public static boolean isOperator(OfflinePlayer player) {
		return player.isOp();
	}

	/**
	 * Checks whether the given player has the specified player flag in the target region.
	 * If the player lacks the permission, a cooldown-gated info message is sent,
	 * except for the TAKE_FALL_DAMAGE flag where no message is shown.
	 * <p>
	 * Resolution order:
	 * 1) If the player is the active renter of the region, permissions are granted for all flags except PVP.
	 * 2) Otherwise, if the player is a member, the member flags are used.
	 * 3) Otherwise, the region's global player flags are used.
	 *
	 * @param regionId the region UUID
	 * @param player   the player to check
	 * @param flag     the PlayerFlags bit to check
	 * @return true if the action is allowed; false otherwise
	 */
	public static boolean hasPermissionFlag(UUID regionId, Player player, long flag, boolean notify) {
		Region region = RegionsManager.findRegion(regionId);
		if (region == null) return true;

		boolean response;

		SerializableRent rent = region.getRent();
		War war = WarsManager.findWarByRegionId(regionId);

		if (rent != null && rent.getPlayerId() != null
				&& rent.getPlayerId().equals(player.getUniqueId()) && !List.of(PlayerFlags.PVP, PlayerFlags.PASSTHROUGH).contains(flag)) {
			response = true;
		} else if (war != null
				&& WarsManager.getMembersOfWar(war.getUniqueId()).stream().map(OfflinePlayer::getUniqueId).toList().contains(player.getUniqueId())
				&& List.of(PlayerFlags.PVP,
				PlayerFlags.DOORS,
				PlayerFlags.TRAP_DOORS,
				PlayerFlags.PASSTHROUGH,
				PlayerFlags.FENCE_GATES,
				PlayerFlags.ELYTRA
		).contains(flag)) {
			response = true;
		} else if (region.isPlayerMember(player)) {
			SerializableMember member = region.getMember(player);
			response = FlagsCalculator.isFlagSet(member.getFlags(), flag);
		} else {
			response = FlagsCalculator.isFlagSet(region.getPlayerFlags(), flag);
		}

		if (!response
				&& flag != PlayerFlags.TAKE_FALL_DAMAGE
				&& !COOLDOWN.contains(player.getUniqueId())
				&& notify) {
			sendDenialMessage(player, region, flag);
		}

		return response;
	}


	public static boolean hasPermissionFlag(UUID regionId,
											UUID subAreaId,
											Player player,
											long flag,
											boolean notify) {

		Region region = RegionsManager.findRegion(regionId);
		if (region == null) {
			return true;
		}

		boolean allowed = calculatePermission(region, subAreaId, player, flag);

		if (!allowed && notify && !COOLDOWN.contains(player.getUniqueId())) {
			sendDenialMessage(player, region, flag);
		}

		return allowed;
	}

	private static boolean calculatePermission(Region region,
											   UUID subAreaId,
											   Player player,
											   long flag) {
		SubArea subArea = subAreaId != null ? SubAreasManager.findSubArea(subAreaId) : null;

		if (subArea != null) {
			SerializableRent subRent = subArea.getRent();
			if (subRent != null
					&& subRent.getPlayerId() != null
					&& subRent.getPlayerId().equals(player.getUniqueId())
					&& !RENT_BLACKLIST.contains(flag)) {
				return true;
			}

			if (subArea.isPlayerMember(player)) {
				SerializableMember member = subArea.getMember(player);

				return FlagsCalculator.isFlagSet(member.getFlags(), flag);
			}

			return FlagsCalculator.isFlagSet(subArea.getFlags(), flag);
		}

		War war = WarsManager.findWarByRegionId(region.getUniqueId());

		if (war != null) {
			List<UUID> warMembers = WarsManager.getMembersOfWar(war.getUniqueId())
					.stream()
					.map(OfflinePlayer::getUniqueId)
					.toList();

			if (warMembers.contains(player.getUniqueId())) {
				Set<Long> warFlags = Set.of(
						PlayerFlags.PVP,
						PlayerFlags.DOORS,
						PlayerFlags.TRAP_DOORS,
						PlayerFlags.PASSTHROUGH,
						PlayerFlags.FENCE_GATES,
						PlayerFlags.ELYTRA
				);
				if (warFlags.contains(flag)) return true;
			}
		}

		SerializableRent regionRent = region.getRent();

		if (regionRent != null
				&& regionRent.getPlayerId() != null
				&& regionRent.getPlayerId().equals(player.getUniqueId())
				&& !RENT_BLACKLIST.contains(flag)) {
			return true;
		}

		if (region.isPlayerMember(player)) {
			SerializableMember member = region.getMember(player);
			return FlagsCalculator.isFlagSet(member.getFlags(), flag);
		}

		return FlagsCalculator.isFlagSet(region.getPlayerFlags(), flag);
	}

	private static void sendDenialMessage(Player player, Region region, long flag) {
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("{flag}", PlayerFlags.from(flag));
		placeholders.put("{region}", region.getName());

		PlayerUtils.sendMessage(player, 50, placeholders);

		COOLDOWN.add(player.getUniqueId());
		Homestead.getInstance().runAsyncTaskLater(() -> COOLDOWN.remove(player.getUniqueId()), MESSAGE_COOLDOWN_SECONDS);
	}

	public static boolean hasControlRegionPermissionFlag(UUID regionId, Player player, long flag) {
		Region region = RegionsManager.findRegion(regionId);

		if (region != null) {
			if (PlayerUtils.isOperator(player) || region.isOwner(player)) {
				return true;
			}

			boolean response = true;

			if (region.isPlayerMember(player)) {
				SerializableMember member = region.getMember(player);

				response = FlagsCalculator.isFlagSet(member.getRegionControlFlags(), flag);
			}

			if (!response && !COOLDOWN.contains(player.getUniqueId())) {
				Map<String, String> replacements = new HashMap<>();
				replacements.put("{flag}", RegionControlFlags.from(flag));
				replacements.put("{region}", region.getName());

				PlayerUtils.sendMessage(player, 70, replacements);

				COOLDOWN.add(player.getUniqueId());

				Homestead.getInstance().runAsyncTaskLater(() -> {
					COOLDOWN.remove(player.getUniqueId());
				}, 3);
			}

			return response;
		}

		return true;
	}

	public static String getPlayerGroup(OfflinePlayer player) {
		if (PlayerLimits.getLimitsMethod() != LimitMethod.GROUPS) {
			return null;
		}

		try {
			if (player.isOnline()) {
				return Homestead.vault.getPermissions().getPrimaryGroup((Player) player);
			} else {
				Location location = player.getLocation();

				if (location == null) {
					return null;
				}

				World world = location.getWorld();

				if (world == null) {
					return null;
				}

				return Homestead.vault.getPermissions().getPrimaryGroup(world.getName(),
						player);
			}
		} catch (UnsupportedOperationException e) {
			Logger.error(
					"Unable to find a service provider for permissions and groups, using the default group \"default\".");
			Logger.error(
					"Please install a plugin that supports permissions and groups. We recommend installing the LuckPerms plugin.");
			Logger.error(
					"To ignore this warning, change the limits method to \"static\" in this setting: limits.method");
		}

		return null;
	}
}
