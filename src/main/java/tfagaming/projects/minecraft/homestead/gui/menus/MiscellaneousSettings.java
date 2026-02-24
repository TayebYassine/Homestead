package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MiscellaneousSettings {

	private static final Map<UUID, UUID> DELETE_CONFIRM_REGION = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> DELETE_CONFIRM_TIME = new ConcurrentHashMap<>();
	private static final long DELETE_CONFIRM_WINDOW_MS = 6000L;

	public MiscellaneousSettings(Player player, Region region) {
		Menu gui = new Menu(MenuUtils.getTitle(12), 9 * 3);

		Placeholder placeholder = new Placeholder()
				.add("{region}", region.getName())
				.add("{region-displayname}", region.getDisplayName())
				.add("{region-description}", region.getDescription());

		gui.addItem(11, MenuUtils.getButton(34, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				region.setName(input);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				RegionsManager.addNewLog(region.getUniqueId(), 0, new Placeholder()
						.add("{executor}", player.getName())
						.add("{newname}", input));
				Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region));
			}, (message) -> {
				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player, RegionControlFlags.RENAME_REGION))
					return false;
				if (!StringUtils.isValidRegionName(message)) {
					Messages.send(player, 1);
					return false;
				}
				if (message.equalsIgnoreCase(region.getName())) {
					Messages.send(player, 11);
					return false;
				}
				if (RegionsManager.isNameUsed(message)) {
					Messages.send(player, 2);
					return false;
				}
				return true;
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region)), 78);
		});

		gui.addItem(12, MenuUtils.getButton(35, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				region.setDisplayName(input);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				RegionsManager.addNewLog(region.getUniqueId(), 6, new Placeholder()
						.add("{executor}", player.getName())
						.add("{newdisplayname}", region.getDisplayName()));
				Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region));
			}, (message) -> {
				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player, RegionControlFlags.RENAME_REGION))
					return false;
				if (!StringUtils.isValidRegionDisplayName(message)) {
					Messages.send(player, 14);
					return false;
				}
				if (region.getDisplayName().equals(message)) {
					Messages.send(player, 11);
					return false;
				}
				return true;
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region)), 79);
		});

		gui.addItem(13, MenuUtils.getButton(36, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				region.setDescription(input);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region));
			}, (message) -> {
				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player, RegionControlFlags.SET_DESCRIPTION))
					return false;
				if (!StringUtils.isValidRegionDescription(message)) {
					Messages.send(player, 16);
					return false;
				}
				if (region.getDescription().equals(message)) {
					Messages.send(player, 11);
					return false;
				}
				return true;
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new MiscellaneousSettings(player, region)), 80);
		});

		gui.addItem(14, MenuUtils.getButton(37, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player, RegionControlFlags.SET_SPAWN))
				return;

			Location location = player.getLocation();
			Chunk chunk = location.getChunk();

			if (ChunksManager.getRegionOwnsTheChunk(chunk) == null
					|| !ChunksManager.getRegionOwnsTheChunk(chunk).getUniqueId().equals(region.getUniqueId())) {
				Messages.send(player, 142);
				return;
			}

			region.setLocation(location);
			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
			RegionsManager.addNewLog(region.getUniqueId(), 1, new Placeholder()
					.add("{executor}", player.getName())
					.add("{location}", Formatter.getLocation(location)));
		});

		gui.addItem(15, MenuUtils.getButton(38, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);
				region.setOwner(targetPlayer);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				if (region.isPlayerMember(targetPlayer)) region.removeMember(targetPlayer);
				if (region.isPlayerInvited(targetPlayer)) region.removePlayerInvite(targetPlayer);
				Homestead.getInstance().runSyncTask(() -> new RegionsMenu(player));
			}, (message) -> {
				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);
				if (target == null) {
					Messages.send(player, 29, new Placeholder().add("{playername}", message));
					return false;
				}
				if (!PlayerUtils.isOperator(player) && !region.isOwner(player)) {
					Messages.send(player, 30);
					return false;
				}
				if (region.isPlayerBanned(target)) {
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

		gui.addItem(22, MenuUtils.getButton(64, placeholder), (_player, event) -> {
			if (!(event.isRightClick() && event.isShiftClick())) return;

			if (!PlayerUtils.isOperator(_player) && !region.isOwner(_player)) {
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
				RegionsManager.deleteRegion(region.getUniqueId(), player);
				PlayerBank.deposit(region.getOwner(), amountToGive);

				Messages.send(player, 6, new Placeholder()
						.add("{region}", region.getDisplayName())
						.add("{region-bank}", Formatter.getBalance(amountToGive)));
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

				new RegionsMenu(_player);
				return;
			}

			DELETE_CONFIRM_REGION.put(pid, region.getUniqueId());
			DELETE_CONFIRM_TIME.put(pid, now);
			Messages.send(player, 158);
		});

		gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new RegionMenu(player, region);
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}
}