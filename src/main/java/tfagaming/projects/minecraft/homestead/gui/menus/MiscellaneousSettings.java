package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionDescriptionUpdateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionDisplaynameUpdateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionRenameEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionTransferOwnershipEvent;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
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

public final class MiscellaneousSettings {

	private static final Map<UUID, UUID> DELETE_CONFIRM_REGION = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> DELETE_CONFIRM_TIME = new ConcurrentHashMap<>();
	private static final long DELETE_CONFIRM_WINDOW_MS = 6000L;

	public MiscellaneousSettings(Player player, Region region) {
		Menu gui = new Menu(MenuUtility.getTitle(12), 9 * 3);

		Placeholder placeholder = new Placeholder()
				.add("{region}", region.getName())
				.add("{region-displayname}", region.getDisplayName())
				.add("{region-description}", region.getDescription());

		gui.addItem(11, MenuUtility.getButton(34, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;

			if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE)) {
				Cooldown.sendCooldownMessage(player);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				final String oldName = region.getName();
				Cooldown.startCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE);
				region.setName(input);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				RegionManager.addNewLog(region.getUniqueId(), 0, new Placeholder()
						.add("{executor}", player.getName())
						.add("{newname}", input));

				RegionRenameEvent _event = new RegionRenameEvent(region, player, oldName, input);
				Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

				Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region));
			}, (message) -> {
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
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region)), 78);
		});

		gui.addItem(12, MenuUtility.getButton(35, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE)) {
				Cooldown.sendCooldownMessage(player);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				final String oldDisplayname = region.getDisplayName();
				Cooldown.startCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE);
				region.setDisplayName(input);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				RegionManager.addNewLog(region.getUniqueId(), 6, new Placeholder()
						.add("{executor}", player.getName())
						.add("{newdisplayname}", region.getDisplayName()));

				RegionDisplaynameUpdateEvent _event = new RegionDisplaynameUpdateEvent(region, player, oldDisplayname, input);
				Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

				Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region));
			}, (message) -> {
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
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region)), 79);
		});

		gui.addItem(13, MenuUtility.getButton(36, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DESCRIPTION_CHANGE)) {
				Cooldown.sendCooldownMessage(player);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				final String oldDescription = region.getDescription();

				Cooldown.startCooldown(player, Cooldown.Type.REGION_DESCRIPTION_CHANGE);

				region.setDescription(input);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

				RegionDescriptionUpdateEvent _event = new RegionDescriptionUpdateEvent(region, player, oldDescription, input);
				Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

				Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region));
			}, (message) -> {
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
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region)), 80);
		});

		gui.addItem(14, MenuUtility.getButton(37, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_SPAWN_CHANGE)) {
				Cooldown.sendCooldownMessage(player);
				return;
			}

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
			RegionManager.addNewLog(region.getUniqueId(), 1, new Placeholder()
					.add("{executor}", player.getName())
					.add("{location}", Formatter.getLocation(location)));
		});

		gui.addItem(15, MenuUtility.getButton(38, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_TRANSFER_OWNERSHIP)) {
				Cooldown.sendCooldownMessage(player);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);
				Cooldown.startCooldown(player, Cooldown.Type.REGION_TRANSFER_OWNERSHIP);
				region.setOwner(targetPlayer);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				if (MemberManager.isMemberOfRegion(region, targetPlayer)) region.removeMember(targetPlayer);
				if (region.isPlayerInvited(targetPlayer)) InviteManager.deleteInvitesOfPlayer(region, targetPlayer);

				RegionTransferOwnershipEvent _event = new RegionTransferOwnershipEvent(region, player, targetPlayer);
				Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

				Homestead.getInstance().runSyncTask(() -> new RegionsMenu(player));
			}, (message) -> {
				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);
				if (target == null) {
					Messages.send(player, 29, new Placeholder().add("{playername}", message));
					return false;
				}
				if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
					Messages.send(player, 30);
					return false;
				}
				if (BannedPlayerManager.isBanned(region, target)) {
					Messages.send(player, 32, new Placeholder().add("{playername}", target.getName()));
					return false;
				}
				if (region.isOwner(target)) {
					Messages.send(player, 30);
					return false;
				}
				return true;
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region)), 81);
		});

		gui.addItem(22, MenuUtility.getButton(64, placeholder), (_player, event) -> {
			if (!(event.isRightClick() && event.isShiftClick())) return;

			if (!PlayerUtility.isOperator(_player) && !region.isOwner(_player)) {
				Messages.send(_player, 159);
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}

			UUID pid = _player.getUniqueId();
			long now = System.currentTimeMillis();
			UUID pendingRegion = DELETE_CONFIRM_REGION.get(pid);
			Long ts = DELETE_CONFIRM_TIME.get(pid);

			if (pendingRegion != null && pendingRegion.equals(region.getUniqueId())
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
		});

		gui.addItem(18, MenuUtility.getBackButton(), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new RegionMenu(player, region);
		});

		gui.open(player, MenuUtility.getEmptySlot());
	}
}