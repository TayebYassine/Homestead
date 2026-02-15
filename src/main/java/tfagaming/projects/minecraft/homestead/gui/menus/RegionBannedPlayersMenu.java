package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.sessions.playerinput.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBannedPlayer;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegionBannedPlayersMenu {
	List<SerializableBannedPlayer> bannedPlayers;

	public RegionBannedPlayersMenu(Player player, Region region) {
		bannedPlayers = region.getBannedPlayers();

		PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(9), 9 * 4,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(), getItems(player, region), (_player, event) -> {
			new ManagePlayersMenu(player, region);
		}, (_player, context) -> {
			if (context.getIndex() >= bannedPlayers.size()) {
				return;
			}

			SerializableBannedPlayer bannedPlayer = bannedPlayers.get(context.getIndex());

			if (context.getEvent().isLeftClick()) {
				if (region.isPlayerBanned(bannedPlayer.getBukkitOfflinePlayer())) {
					if (!player.hasPermission("homestead.region.players.unban")) {
						Messages.send(player, 8);
						return;
					}

					if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
							RegionControlFlags.UNBAN_PLAYERS)) {
						return;
					}

					region.unbanPlayer(bannedPlayer.getBukkitOfflinePlayer());

					PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

					PaginationMenu instance = context.getInstance();

					bannedPlayers = region.getBannedPlayers();

					instance.setItems(getItems(player, region));
				}
			}
		});

		gui.addActionButton(0, MenuUtils.getButton(28), (_player, event) -> {
			if (!event.isLeftClick()) {
				return;
			}

			if (!player.hasPermission("homestead.region.players.ban")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

				region.banPlayer(targetPlayer);

				if (region.isPlayerMember(targetPlayer)) {
					region.removeMember(targetPlayer);
				}

				if (region.isPlayerInvited(targetPlayer)) {
					region.removePlayerInvite(targetPlayer);
				}

				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

				Homestead.getInstance().runSyncTask(() -> {
					new RegionBannedPlayersMenu(player, region);
				});
			}, (message) -> {
				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

				if (target == null) {
					Messages.send(player, 29, new Placeholder()
							.add("{playername}", message)
					);
					return false;
				}

				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.BAN_PLAYERS)) {
					return false;
				}

				if (region.isPlayerBanned(target)) {
					Messages.send(player, 32, new Placeholder()
							.add("{playername}", target.getName())
					);
					return false;
				}

				if (region.isOwner(target)) {
					Messages.send(player, 30);
					return false;
				}

				SerializableRent rent = region.getRent();

				if (rent != null && rent.getPlayerId().equals(target.getUniqueId())) {
					Messages.send(player, 196);
					return true;
				}

				return true;
			}, (__player) -> {
				Homestead.getInstance().runSyncTask(() -> {
					new RegionBannedPlayersMenu(player, region);
				});
			}, 73);
		});

		gui.addActionButton(2, MenuUtils.getButton(32), (_player, event) -> {
			if (!event.isLeftClick()) {
				return;
			}

			if (!player.hasPermission("homestead.region.players.unban")) {
				Messages.send(player, 8);
				return;
			}

			if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
					RegionControlFlags.UNBAN_PLAYERS)) {
				return;
			}

			if (region.getBannedPlayers().isEmpty()) {
				Messages.send(player, 77);
				return;
			}

			region.setBannedPlayers(new ArrayList<>());

			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

			Messages.send(player, 94);

			Homestead.getInstance().runSyncTask(() -> {
				new RegionBannedPlayersMenu(player, region);
			});
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	public List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < bannedPlayers.size(); i++) {
			SerializableBannedPlayer bannedPlayer = bannedPlayers.get(i);

			HashMap<String, String> replacements = new HashMap<>();

			replacements.put("{region}", region.getName());
			replacements.put("{playername}", bannedPlayer.getBukkitOfflinePlayer().getName());
			replacements.put("{player-bannedat}", Formatters.getDate(bannedPlayer.getBannedAt()));
			replacements.put("{player-banreason}", bannedPlayer.getReason());

			items.add(MenuUtils.getButton(27, replacements, bannedPlayer.getBukkitOfflinePlayer()));
		}

		return items;
	}
}
