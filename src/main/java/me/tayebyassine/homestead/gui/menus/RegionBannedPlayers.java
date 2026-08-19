package me.tayebyassine.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.BanPlayerEvent;
import me.tayebyassine.homestead.api.events.BulkUnbanPlayersEvent;
import me.tayebyassine.homestead.api.events.UnbanPlayerEvent;
import me.tayebyassine.homestead.flags.ControlFlags;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.managers.*;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionBan;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.sessions.PlayerInputSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.menus.MenuUtility;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class RegionBannedPlayers {
	private List<RegionBan> bannedPlayers;

	public RegionBannedPlayers(Player player, Region region) {
		this.bannedPlayers = BanManager.getBansOfRegion(region);

		PaginationMenu gui = PaginationMenu.builder(MenuUtility.getTitle(9), 9 * 4)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(getItems(player, region))
				.fillEmptySlots()
				.goBack((_player, event) -> new RegionPlayersManagement(player, region))
				.onClick((_player, context) -> handleUnban(player, region, context))
				.build();

		gui.addActionButton(0, MenuUtility.getButton(28), handleBanPlayer(player, region))
				.addActionButton(2, MenuUtility.getButton(32), handleUnbanAll(player, region));

		gui.open(player);
	}

	private static BiConsumer<Player, InventoryClickEvent> handleBanPlayer(Player player, Region region) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!player.hasPermission("homestead.actions.regions.players.ban")) {
				Messages.send(player, "common.no_permission");
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}

			player.closeInventory();

			PlayerInputSession.builder(Homestead.getInstance(), player)
					.prompt(73)
					.validator(msg -> validateBan(player, region, msg))
					.callback((p, input) -> {
						OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

						if (targetPlayer == null) return;

						BanManager.banPlayer(region, targetPlayer, null);
						if (MemberManager.isMemberOfRegion(region, targetPlayer))
							MemberManager.removeMemberFromRegion(targetPlayer, region);
						if (InviteManager.isInvited(region, targetPlayer))
							InviteManager.deleteInvitesOfPlayer(region, targetPlayer);

						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

						LogManager.addLog(region, player, LogManager.PredefinedLog.BAN_PLAYER, targetPlayer.getName());

						Homestead.callEvent(new BanPlayerEvent(region, player, null));

						Homestead.getInstance().runSyncTask(() -> new RegionBannedPlayers(player, region));
					})
					.onCancel(p -> Homestead.getInstance().runSyncTask(() -> new RegionBannedPlayers(player, region)))
					.build();
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleUnbanAll(Player player, Region region) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!player.hasPermission("homestead.actions.regions.players.unban")) {
				Messages.send(player, "common.no_permission");
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}

			if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
					ControlFlags.UNBAN_PLAYERS)) {
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}

			int bannedCount = BanManager.getBansOfRegion(region).size();
			if (bannedCount == 0) {
				Messages.send(player, "commands.unban.5");
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}

			BanManager.unbanAllPlayers(region);

			LogManager.addLog(region, player, LogManager.PredefinedLog.PURGE_BANS);

			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

			Homestead.callEvent(new BulkUnbanPlayersEvent(region));

			Homestead.getInstance().runSyncTask(() -> new RegionBannedPlayers(player, region));
		};
	}

	private static boolean validateBan(Player player, Region region, String message) {
		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

		if (target == null) {
			Messages.send(player, "commands.ban.3", message);
			return false;
		}
		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.BAN_PLAYERS)) {
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return false;
		}
		if (BanManager.isBanned(region, target)) {
			Messages.send(player, "commands.ban.5", target.getName());
			return false;
		}
		if (region.isOwner(target) || PlayerUtility.equals(player, target)) {
			Messages.send(player, "commands.ban.4");
			return false;
		}
		SeRent rent = region.getRent();
		if (rent != null && rent.getRenterId().equals(target.getUniqueId())) {
			Messages.send(player, "commands.ban.6", target.getName());
			return false;
		}
		return true;
	}

	private void handleUnban(Player player, Region region, PaginationMenu.ClickContext context) {
		if (context.getIndex() >= bannedPlayers.size()) return;

		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		if (!player.hasPermission("homestead.actions.regions.players.unban")) {
			Messages.send(player, "common.no_permission");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		RegionBan bannedPlayer = bannedPlayers.get(context.getIndex());
		if (!context.getEvent().isLeftClick()) return;
		if (!BanManager.isBanned(region, bannedPlayer.getPlayer())) return;

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.UNBAN_PLAYERS)) {
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		BanManager.unbanPlayer(region, bannedPlayer.getPlayer());
		PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

		LogManager.addLog(region, player, LogManager.PredefinedLog.UNBAN_PLAYER, bannedPlayer.getPlayerName());

		Homestead.callEvent(new UnbanPlayerEvent(region, player));

		bannedPlayers = BanManager.getBansOfRegion(region);
		context.getInstance().setItems(getItems(player, region));
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (RegionBan bannedPlayer : bannedPlayers) {
			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", bannedPlayer.getPlayerName())
					.add("{player-bannedat}", Formatter.getDate(bannedPlayer.getBannedAt()))
					.add("{player-banreason}", wrapMessage(bannedPlayer.getReason()));

			items.add(MenuUtility.getButton(27, placeholder, bannedPlayer.getPlayer()));
		}

		return items;
	}

	private String wrapMessage(String message) {
		message = ColorTranslator.preserve(message);

		int wrapLength = 40;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < message.length(); i++) {
			if (i > 0 && i % wrapLength == 0) sb.append("\n");
			sb.append(message.charAt(i));
		}

		return sb.toString();
	}
}