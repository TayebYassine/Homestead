package tfagaming.projects.minecraft.homestead.tools.minecraft.players;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits.LimitMethod;
import tfagaming.projects.minecraft.homestead.tools.minecraft.platform.PlatformBridge;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerUtils {

	private static final int MESSAGE_COOLDOWN_SECONDS = 3;
	private static final HashSet<UUID> COOLDOWN = new HashSet<UUID>();
	private static final Set<Long> RENT_BLACKLIST = Set.of(
			PlayerFlags.PVP,
			PlayerFlags.PASSTHROUGH
	);

	public static void sendMessageRegionEnter(Player player, Placeholder placeholder) {
		String type = Homestead.config.getString("enter-exit-region-message.type").toLowerCase();

		switch (type) {
			case "title": {
				List<String> titleData = Homestead.config.getStringList(
						"enter-exit-region-message.messages.enter.title");

				if (titleData.size() == 2) {
					String t1 = ColorTranslator.translate(
							Formatter.applyPlaceholders(titleData.getFirst(), placeholder));
					String t2 = ColorTranslator.translate(
							Formatter.applyPlaceholders(titleData.get(1), placeholder));

					PlatformBridge.get().showTitle(player, t1, t2, 10, 70, 20);
				}
				break;
			}
			case "actionbar": {
				String text = ColorTranslator.translate(
						Formatter.applyPlaceholders(
								Homestead.config.getString(
										"enter-exit-region-message.messages.enter.actionbar"),
								placeholder));

				PlatformBridge.get().sendActionBar(player, text);
				break;
			}
			default: {
				String text = ColorTranslator.translate(
						Formatter.applyPlaceholders(
								Homestead.config.getString(
										"enter-exit-region-message.messages.enter.chat"),
								placeholder));

				PlatformBridge.get().sendMessage(player, text);
				break;
			}
		}
	}

	public static void sendMessageRegionExit(Player player, Placeholder placeholder) {
		String type = Homestead.config.getString("enter-exit-region-message.type").toLowerCase();

		switch (type) {
			case "title": {
				List<String> titleData = Homestead.config.getStringList(
						"enter-exit-region-message.messages.exit.title");

				if (titleData.size() == 2) {
					String t1 = ColorTranslator.translate(
							Formatter.applyPlaceholders(titleData.getFirst(), placeholder));
					String t2 = ColorTranslator.translate(
							Formatter.applyPlaceholders(titleData.get(1), placeholder));

					PlatformBridge.get().showTitle(player, t1, t2, 10, 70, 20);
				}
				break;
			}
			case "actionbar": {
				String text = ColorTranslator.translate(
						Formatter.applyPlaceholders(
								Homestead.config.getString(
										"enter-exit-region-message.messages.exit.actionbar"),
								placeholder));

				PlatformBridge.get().sendActionBar(player, text);
				break;
			}
			default: {
				String text = ColorTranslator.translate(
						Formatter.applyPlaceholders(
								Homestead.config.getString(
										"enter-exit-region-message.messages.exit.chat"),
								placeholder));

				PlatformBridge.get().sendMessage(player, text);
				break;
			}
		}
	}

	public static void teleportPlayerToChunk(Player player, Chunk chunk) {
		Location location = ChunkManager.getLocation(player, chunk);
		player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
	}

	public static boolean isOperator(Player player) {
		if (player.isOp()) return true;
		return player.hasPermission("homestead.operator");
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
	 * @param player   the player to fetch
	 * @param flag     the PlayerFlags bit to fetch
	 * @return true if the action is allowed; false otherwise
	 */
	public static boolean hasPermissionFlag(UUID regionId, Player player, long flag, boolean notify) {
		Region region = RegionManager.findRegion(regionId);
		if (region == null) return true;

		boolean response;

		SerializableRent rent = region.getRent();
		War war = WarManager.findWarByRegionId(regionId);

		if (rent != null && rent.getPlayerId() != null
				&& rent.getPlayerId().equals(player.getUniqueId())
				&& !List.of(PlayerFlags.PVP, PlayerFlags.PASSTHROUGH).contains(flag)) {
			response = true;
		} else if (war != null
				&& WarManager.getMembersOfWar(war.getUniqueId()).stream()
				.map(OfflinePlayer::getUniqueId).toList().contains(player.getUniqueId())
				&& List.of(PlayerFlags.PVP, PlayerFlags.DOORS, PlayerFlags.TRAP_DOORS,
				PlayerFlags.PASSTHROUGH, PlayerFlags.FENCE_GATES, PlayerFlags.ELYTRA).contains(flag)) {
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

	public static boolean hasPermissionFlag(UUID regionId, UUID subAreaId,
											Player player, long flag, boolean notify) {
		Region region = RegionManager.findRegion(regionId);
		if (region == null) return true;

		boolean allowed = calculatePermission(region, subAreaId, player, flag);

		if (!allowed && notify && !COOLDOWN.contains(player.getUniqueId())) {
			sendDenialMessage(player, region, flag);
		}

		return allowed;
	}

	private static boolean calculatePermission(Region region, UUID subAreaId,
											   Player player, long flag) {
		SubArea subArea = subAreaId != null ? SubAreaManager.findSubArea(subAreaId) : null;

		if (subArea != null) {
			SerializableRent subRent = subArea.getRent();
			if (subRent != null && subRent.getPlayerId() != null
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

		War war = WarManager.findWarByRegionId(region.getUniqueId());

		if (war != null) {
			List<UUID> warMembers = WarManager.getMembersOfWar(war.getUniqueId())
					.stream().map(OfflinePlayer::getUniqueId).toList();

			if (warMembers.contains(player.getUniqueId())) {
				Set<Long> warFlags = Set.of(PlayerFlags.PVP, PlayerFlags.DOORS,
						PlayerFlags.TRAP_DOORS, PlayerFlags.PASSTHROUGH,
						PlayerFlags.FENCE_GATES, PlayerFlags.ELYTRA);
				if (warFlags.contains(flag)) return true;
			}
		}

		SerializableRent regionRent = region.getRent();

		if (regionRent != null && regionRent.getPlayerId() != null
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
		Messages.send(player, 50, new Placeholder()
				.add("{flag}", PlayerFlags.from(flag))
				.add("{region}", region.getName())
		);

		COOLDOWN.add(player.getUniqueId());
		Homestead.getInstance().runAsyncTaskLater(() -> COOLDOWN.remove(player.getUniqueId()),
				MESSAGE_COOLDOWN_SECONDS);
	}

	public static boolean hasControlRegionPermissionFlag(UUID regionId, Player player, long flag) {
		Region region = RegionManager.findRegion(regionId);

		if (region != null) {
			if (PlayerUtils.isOperator(player) || region.isOwner(player)) return true;

			boolean response = true;

			if (region.isPlayerMember(player)) {
				SerializableMember member = region.getMember(player);
				response = FlagsCalculator.isFlagSet(member.getRegionControlFlags(), flag);
			}

			if (!response && !COOLDOWN.contains(player.getUniqueId())) {
				Messages.send(player, 70, new Placeholder()
						.add("{flag}", RegionControlFlags.from(flag))
						.add("{region}", region.getName())
				);

				COOLDOWN.add(player.getUniqueId());
				Homestead.getInstance().runAsyncTaskLater(() -> COOLDOWN.remove(player.getUniqueId()), 3);
			}

			return response;
		}

		return true;
	}

	public static String getPlayerGroup(OfflinePlayer player) {
		if (Limits.getLimitsMethod() != LimitMethod.GROUPS) return null;

		try {
			if (player.isOnline()) {
				return Homestead.vault.getPermissions().getPrimaryGroup(player);
			} else {
				return null;
			}
		} catch (Exception e) {
			Logger.error("Unable to find a service provider for permissions and groups, using the default group \"default\".");
			Logger.error("Please install a plugin that supports permissions and groups. We recommend installing the LuckPerms plugin.");
			Logger.error("To ignore this warning, change the limits method to \"static\" in this setting: limits.method");
		}

		return null;
	}
}