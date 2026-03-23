package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionBanPlayerEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionBulkUnbanPlayersEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionDisplaynameUpdateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionUnbanPlayerEvent;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBannedPlayer;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegionBannedPlayers {
	private List<SerializableBannedPlayer> bannedPlayers;

	public RegionBannedPlayers(Player player, Region region) {
		bannedPlayers = region.getBannedPlayers();

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(9), 9 * 4,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				getItems(player, region),
				(_player, event) -> new RegionPlayersManagement(player, region),
				(_player, context) -> {
					if (context.getIndex() >= bannedPlayers.size()) return;

					SerializableBannedPlayer bannedPlayer = bannedPlayers.get(context.getIndex());

					if (!context.getEvent().isLeftClick()) return;

					if (!region.isPlayerBanned(bannedPlayer.bukkit())) return;

					if (!player.hasPermission("homestead.region.players.unban")) {
						Messages.send(player, 8);
						return;
					}

					if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
							RegionControlFlags.UNBAN_PLAYERS)) {
						return;
					}

					region.unbanPlayer(bannedPlayer.bukkit());
					PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

					RegionUnbanPlayerEvent _event = new RegionUnbanPlayerEvent(region, player, bannedPlayer.bukkit());
					Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

					bannedPlayers = region.getBannedPlayers();
					context.getInstance().setItems(getItems(player, region));
				});

		gui.addActionButton(0, MenuUtils.getButton(28), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.players.ban")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

				region.banPlayer(targetPlayer);
				if (region.isPlayerMember(targetPlayer)) region.removeMember(targetPlayer);
				if (region.isPlayerInvited(targetPlayer)) region.removePlayerInvite(targetPlayer);

				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

				RegionBanPlayerEvent _event = new RegionBanPlayerEvent(region, player, targetPlayer, null);
				Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

				Homestead.getInstance().runSyncTask(() -> new RegionBannedPlayers(player, region));
			}, (message) -> {
				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

				if (target == null) {
					Messages.send(player, 29, new Placeholder().add("{playername}", message));
					return false;
				}
				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.BAN_PLAYERS)) {
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
				SerializableRent rent = region.getRent();
				if (rent != null && rent.getPlayerId().equals(target.getUniqueId())) {
					Messages.send(player, 196);
				}
				return true;
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new RegionBannedPlayers(player, region)), 73);
		});

		gui.addActionButton(2, MenuUtils.getButton(32), (_player, event) -> {
			if (!event.isLeftClick()) return;

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

			final List<OfflinePlayer> oldBannedPlayers = region.getBannedPlayers().stream().map(SerializableBannedPlayer::bukkit).toList();

			region.setBannedPlayers(new ArrayList<>());
			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
			Messages.send(player, 94);

			RegionBulkUnbanPlayersEvent _event = new RegionBulkUnbanPlayersEvent(region, player, oldBannedPlayers);
			Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

			Homestead.getInstance().runSyncTask(() -> new RegionBannedPlayers(player, region));
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (SerializableBannedPlayer bannedPlayer : bannedPlayers) {
			OfflinePlayer bannedPlayerBukkit = bannedPlayer.bukkit();

			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", bannedPlayerBukkit == null ? "?" : bannedPlayerBukkit.getName())
					.add("{player-bannedat}", Formatter.getDate(bannedPlayer.getBannedAt()))
					.add("{player-banreason}", bannedPlayer.getReason());

			items.add(MenuUtils.getButton(27, placeholder, bannedPlayer.bukkit()));
		}

		return items;
	}
}