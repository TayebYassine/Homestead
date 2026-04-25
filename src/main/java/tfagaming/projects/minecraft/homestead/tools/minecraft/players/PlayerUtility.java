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
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.models.SubArea;
import tfagaming.projects.minecraft.homestead.models.War;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;





import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits.LimitMethod;
import tfagaming.projects.minecraft.homestead.tools.minecraft.platform.PlatformBridge;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class PlayerUtility {
	private PlayerUtility() {
	}

	public static final Set<Long> RENT_FLAGS_SET = Set.of(
			PlayerFlags.PVP
	);
	public static final Set<Long> WAR_FLAGS_SET = Set.of(
			PlayerFlags.PVP,
			PlayerFlags.DOORS,
			PlayerFlags.TRAP_DOORS,
			PlayerFlags.FENCE_GATES,
			PlayerFlags.PASSTHROUGH,
			PlayerFlags.ELYTRA,
			PlayerFlags.TELEPORT,
			PlayerFlags.PICKUP_ITEMS,
			PlayerFlags.TAKE_FALL_DAMAGE
	);
	private static final int MESSAGE_COOLDOWN_SECONDS = 3;
	private static final HashSet<UUID> COOLDOWN = new HashSet<UUID>();

	public static void sendMessageRegionEnter(Player player, Placeholder placeholder) {
		String type = Resources.<RegionsFile>get(ResourceType.Regions).getString("enter-exit-region-message.type").toLowerCase();

		switch (type) {
			case "title": {
				List<String> titleData = Resources.<RegionsFile>get(ResourceType.Regions).getStringList(
						"enter-exit-region-message.messages.enter.title");

				if (titleData.size() == 2) {
					String t1 = Formatter.applyPlaceholders(titleData.getFirst(), placeholder);
					String t2 = Formatter.applyPlaceholders(titleData.get(1), placeholder);

					PlatformBridge.get().showTitle(player, t1, t2, 10, 70, 20);
				}
				break;
			}
			case "actionbar": {
				String text = Formatter.applyPlaceholders(
						Resources.<RegionsFile>get(ResourceType.Regions).getString(
								"enter-exit-region-message.messages.enter.actionbar"),
						placeholder);

				PlatformBridge.get().sendActionBar(player, text);
				break;
			}
			default: {
				String text = Formatter.applyPlaceholders(
						Resources.<RegionsFile>get(ResourceType.Regions).getString(
								"enter-exit-region-message.messages.enter.chat"),
						placeholder);

				PlatformBridge.get().sendMessage(player, text);
				break;
			}
		}
	}

	public static void sendMessageRegionExit(Player player, Placeholder placeholder) {
		String type = Resources.<RegionsFile>get(ResourceType.Regions).getString("enter-exit-region-message.type").toLowerCase();

		switch (type) {
			case "title": {
				List<String> titleData = Resources.<RegionsFile>get(ResourceType.Regions).getStringList(
						"enter-exit-region-message.messages.exit.title");

				if (titleData.size() == 2) {
					String t1 = Formatter.applyPlaceholders(titleData.getFirst(), placeholder);
					String t2 = Formatter.applyPlaceholders(titleData.get(1), placeholder);

					PlatformBridge.get().showTitle(player, t1, t2, 10, 70, 20);
				}
				break;
			}
			case "actionbar": {
				String text = Formatter.applyPlaceholders(
						Resources.<RegionsFile>get(ResourceType.Regions).getString(
								"enter-exit-region-message.messages.exit.actionbar"),
						placeholder);

				PlatformBridge.get().sendActionBar(player, text);
				break;
			}
			default: {
				String text = Formatter.applyPlaceholders(
						Resources.<RegionsFile>get(ResourceType.Regions).getString(
								"enter-exit-region-message.messages.exit.chat"),
						placeholder);

				PlatformBridge.get().sendMessage(player, text);
				break;
			}
		}
	}

	public static void teleportPlayerToChunk(Player player, Chunk chunk) {
		Location location = ChunkUtility.getLocation(player, chunk);

		teleportPlayer(player, location);
	}

	public static void teleportPlayer(Player player, Location location) {
		if (location == null) return;

		if (Homestead.isFolia()) {
			player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
		} else {
			player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
		}
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
	 * except for the {@code TAKE_FALL_DAMAGE} flag where no message is shown.
	 * <p>
	 * Resolution order:<br>
	 * 1. If the player is the active renter of the region, permissions are granted for all flags except {@code PVP}.<br>
	 * 2. Otherwise, if the player is a member, the member flags are used.<br>
	 * 3. Otherwise, the region's global player flags are used.<br>
	 *
	 * @param regionId The region ID
	 * @param player The player to fetch
	 * @param flag The PlayerFlags bit to fetch
	 * @return {@code true} If the action is allowed; {@code false} otherwise
	 */
	public static boolean hasPermissionFlag(long regionId, Player player, long flag, boolean notify) {
		Region region = RegionManager.findRegion(regionId);
		if (region == null) return true;

		boolean response;

		SeRent rent = region.getRent();
		War war = WarManager.findWarByRegion(regionId);

		if (rent != null && rent.getRenterId() != null
				&& rent.getRenterId().equals(player.getUniqueId())
				&& !RENT_FLAGS_SET.contains(flag)) {
			response = true;
		} else if (WarManager.isPlayerInWar(player, war)
				&& WAR_FLAGS_SET.contains(flag)) {
			response = true;
		} else if (MemberManager.isMemberOfRegion(regionId, player)) {
			RegionMember member = MemberManager.getMemberOfRegion(regionId, player);
			response = FlagsCalculator.isFlagSet(member.getPlayerFlags(), flag);
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

	public static boolean hasPermissionFlag(long regionId, long subAreaId,
											Player player, long flag, boolean notify) {
		Region region = RegionManager.findRegion(regionId);
		if (region == null) return true;

		SubArea subArea = SubAreaManager.findSubArea(subAreaId);

		if (subArea != null) {
			SeRent subRent = subArea.getRent();
			if (subRent != null && subRent.getRenterId() != null
					&& subRent.getRenterId().equals(player.getUniqueId())
					&& !RENT_FLAGS_SET.contains(flag)) {
				return true;
			}

			if (MemberManager.isMemberOfSubArea(subAreaId, player)) {
				RegionMember member = MemberManager.getMemberOfSubArea(subAreaId, player);
				return FlagsCalculator.isFlagSet(member.getPlayerFlags(), flag);
			}

			return FlagsCalculator.isFlagSet(subArea.getPlayerFlags(), flag);
		}

		return hasPermissionFlag(regionId, player, flag, notify);
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

	public static boolean hasControlRegionPermissionFlag(long regionId, Player player, long flag) {
		Region region = RegionManager.findRegion(regionId);

		if (region != null) {
			if (PlayerUtility.isOperator(player) || region.isOwner(player)) return true;

			boolean response = true;

			if (MemberManager.isMemberOfRegion(region, player)) {
				RegionMember member = MemberManager.getMemberOfRegion(region, player);
				response = FlagsCalculator.isFlagSet(member.getControlFlags(), flag);
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
			Logger.error("[Services] Unable to find a service provider for permissions and groups, using the default group \"default\".");
			Logger.error("[Services] Please install a plugin that supports permissions and groups. We recommend installing the LuckPerms plugin.");
			Logger.error("[Services] To ignore this warning, change the limits method to \"static\" in this setting: limits.method");
		}

		return null;
	}
}