package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionDescriptionUpdateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionDisplaynameUpdateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionRenameEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionTransferOwnershipEvent;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;

import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class MiscellaneousSettings {

	private static final Map<UUID, Long> DELETE_CONFIRM_REGION = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> DELETE_CONFIRM_TIME = new ConcurrentHashMap<>();
	private static final long DELETE_CONFIRM_WINDOW_MS = 6000L;

	public MiscellaneousSettings(Player player, Region region) {
		Placeholder placeholder = new Placeholder()
				.add("{region}", region.getName())
				.add("{region-displayname}", region.getDisplayName())
				.add("{region-description}", region.getDescription());

		Menu.builder(12, 9 * 3)
				.button(11, MenuUtility.getButton(34, placeholder), handleRename(player, region))
				.button(12, MenuUtility.getButton(35, placeholder), handleDisplayName(player, region))
				.button(13, MenuUtility.getButton(36, placeholder), handleDescription(player, region))
				.button(14, MenuUtility.getButton(37, placeholder), handleSetSpawn(player, region))
				.button(15, MenuUtility.getButton(38, placeholder), handleTransferOwnership(player, region))
				.button(22, MenuUtility.getButton(64, placeholder), handleDeleteRegion(player, region))
				.button(18, MenuUtility.getBackButton(), handleBack(player, region))
				.fillEmptySlots()
				.build()
				.open(player);
	}

	private static BiConsumer<Player, InventoryClickEvent> handleRename(Player player, Region region) {
		return (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}
			if (!event.isLeftClick()) return;
			if (onCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE)) return;

			player.closeInventory();

			PlayerInputSession.builder(Homestead.getInstance(), player)
					.prompt(78)
					.validator(msg -> validateRename(player, region, msg))
					.callback((p, input) -> {
						String oldName = region.getName();
						Cooldown.startCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE);
						region.setName(input);
						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

						RegionRenameEvent _event = new RegionRenameEvent(region, player, oldName, input);
						Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
						Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region));
					})
					.onCancel(p -> Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region)))
					.build();
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleDisplayName(Player player, Region region) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;
			if (onCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE)) return;

			player.closeInventory();

			PlayerInputSession.builder(Homestead.getInstance(), player)
					.prompt(79)
					.validator(msg -> validateDisplayName(player, region, msg))
					.callback((p, input) -> {
						String oldDisplayname = region.getDisplayName();
						Cooldown.startCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE);
						region.setDisplayName(input);
						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

						RegionDisplaynameUpdateEvent _event = new RegionDisplaynameUpdateEvent(region, player, oldDisplayname, input);
						Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
						Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region));
					})
					.onCancel(p -> Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region)))
					.build();
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleDescription(Player player, Region region) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;
			if (onCooldown(player, Cooldown.Type.REGION_DESCRIPTION_CHANGE)) return;

			player.closeInventory();

			PlayerInputSession.builder(Homestead.getInstance(), player)
					.prompt(80)
					.validator(msg -> validateDescription(player, region, msg))
					.callback((p, input) -> {
						String oldDescription = region.getDescription();
						Cooldown.startCooldown(player, Cooldown.Type.REGION_DESCRIPTION_CHANGE);
						region.setDescription(input);
						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

						RegionDescriptionUpdateEvent _event = new RegionDescriptionUpdateEvent(region, player, oldDescription, input);
						Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
						Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region));
					})
					.onCancel(p -> Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region)))
					.build();
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleSetSpawn(Player player, Region region) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;
			if (onCooldown(player, Cooldown.Type.REGION_SPAWN_CHANGE)) return;
			if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player, RegionControlFlags.SET_SPAWN))
				return;

			Location location = player.getLocation();
			Chunk chunk = location.getChunk();

			if (!ChunkManager.isChunkClaimedByRegion(region, chunk)) {
				Messages.send(player, 142);
				return;
			}

			Cooldown.startCooldown(player, Cooldown.Type.REGION_SPAWN_CHANGE);
			region.setLocation(location);
			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleTransferOwnership(Player player, Region region) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;
			if (onCooldown(player, Cooldown.Type.REGION_TRANSFER_OWNERSHIP)) return;

			player.closeInventory();

			PlayerInputSession.builder(Homestead.getInstance(), player)
					.prompt(81)
					.validator(msg -> validateTransferOwnership(player, region, msg))
					.callback((p, input) -> {
						OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

						if (targetPlayer == null) return;

						Cooldown.startCooldown(player, Cooldown.Type.REGION_TRANSFER_OWNERSHIP);
						region.setOwner(targetPlayer);
						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

						if (MemberManager.isMemberOfRegion(region, targetPlayer)) MemberManager.removeMemberFromRegion(targetPlayer, region);
						if (InviteManager.isInvited(region, targetPlayer)) InviteManager.deleteInvitesOfPlayer(region, targetPlayer);

						RegionTransferOwnershipEvent _event = new RegionTransferOwnershipEvent(region, player, targetPlayer);
						Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
						Homestead.getInstance().runSyncTask(() -> new RegionsMenu(player));
					})
					.onCancel(p -> Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region)))
					.build();
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleDeleteRegion(Player player, Region region) {
		return (_player, event) -> {
			if (!(event.isRightClick() && event.isShiftClick())) return;

			if (!PlayerUtility.isOperator(_player) && !region.isOwner(_player)) {
				Messages.send(_player, 159);
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}

			UUID pid = _player.getUniqueId();
			long now = System.currentTimeMillis();
			long pendingRegionId = DELETE_CONFIRM_REGION.get(pid);
			Long ts = DELETE_CONFIRM_TIME.get(pid);

			if (pendingRegionId == region.getUniqueId()
					&& ts != null && (now - ts) <= DELETE_CONFIRM_WINDOW_MS) {
				DELETE_CONFIRM_REGION.remove(pid);
				DELETE_CONFIRM_TIME.remove(pid);

				double amountToGive = region.getBank();
				RegionManager.deleteRegion(region.getUniqueId(), player);
				PlayerBank.deposit(region.getOwner(), amountToGive);

				Messages.send(player, 6, new Placeholder()
						.add("{region}", region.getDisplayName())
						.add("{region-bank}", Formatter.getBalance(amountToGive)));
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

				TargetRegionSession.randomizeRegion(player);
				new RegionsMenu(_player);
				return;
			}

			DELETE_CONFIRM_REGION.put(pid, region.getUniqueId());
			DELETE_CONFIRM_TIME.put(pid, now);
			Messages.send(player, 158);
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleBack(Player player, Region region) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;
			new RegionMenu(player, region);
		};
	}

	private static boolean validateRename(Player player, Region region, String message) {
		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player, RegionControlFlags.RENAME_REGION))
			return false;
		if (!StringUtils.isValidRegionName(message)) {
			Messages.send(player, 1);
			return false;
		}
		if (message.equalsIgnoreCase(region.getName())) {
			Messages.send(player, 11);
			return false;
		}
		if (RegionManager.isNameUsed(message)) {
			Messages.send(player, 2);
			return false;
		}
		if (ColorTranslator.containsMiniMessageTag(message)) {
			Messages.send(player, 30);
			return false;
		}
		return true;
	}

	private static boolean validateDisplayName(Player player, Region region, String message) {
		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player, RegionControlFlags.RENAME_REGION))
			return false;
		if (!StringUtils.isValidRegionDisplayName(message)) {
			Messages.send(player, 14);
			return false;
		}
		if (region.getDisplayName().equals(message)) {
			Messages.send(player, 11);
			return false;
		}
		if (ColorTranslator.containsMiniMessageTag(message)) {
			Messages.send(player, 30);
			return false;
		}
		return true;
	}

	private static boolean validateDescription(Player player, Region region, String message) {
		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player, RegionControlFlags.SET_DESCRIPTION))
			return false;
		if (!StringUtils.isValidRegionDescription(message)) {
			Messages.send(player, 16);
			return false;
		}
		if (region.getDescription().equals(message)) {
			Messages.send(player, 11);
			return false;
		}
		if (ColorTranslator.containsMiniMessageTag(message)) {
			Messages.send(player, 30);
			return false;
		}
		return true;
	}

	private static boolean validateTransferOwnership(Player player, Region region, String message) {
		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);
		if (target == null) {
			Messages.send(player, 29, new Placeholder().add("{playername}", message));
			return false;
		}
		if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, 30);
			return false;
		}
		if (BanManager.isBanned(region, target)) {
			Messages.send(player, 32, new Placeholder().add("{playername}", target.getName()));
			return false;
		}
		if (region.isOwner(target)) {
			Messages.send(player, 30);
			return false;
		}
		return true;
	}

	private static boolean onCooldown(Player player, Cooldown.Type type) {
		if (Cooldown.hasCooldown(player, type)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}
		return false;
	}
}