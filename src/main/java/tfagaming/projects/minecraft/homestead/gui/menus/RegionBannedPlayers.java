package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.BanPlayerEvent;
import tfagaming.projects.minecraft.homestead.api.events.BulkUnbanPlayersEvent;
import tfagaming.projects.minecraft.homestead.api.events.UnbanPlayerEvent;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionBan;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;

import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

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

	private void handleUnban(Player player, Region region, PaginationMenu.ClickContext context) {
		if (context.getIndex() >= bannedPlayers.size()) return;

		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		RegionBan bannedPlayer = bannedPlayers.get(context.getIndex());
		if (!context.getEvent().isLeftClick()) return;
		if (!BanManager.isBanned(region, bannedPlayer.getPlayer())) return;

		if (!player.hasPermission("homestead.region.players.unban")) {
			Messages.send(player, 8);
			return;
		}
		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.UNBAN_PLAYERS)) {
			return;
		}

		BanManager.unbanPlayer(region, bannedPlayer.getPlayer());
		PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

		LogManager.addLog(region, player, LogManager.PredefinedLog.UNBAN_PLAYER, bannedPlayer.getPlayerName());

		Homestead.callEvent(new UnbanPlayerEvent(region, player));

		bannedPlayers = BanManager.getBansOfRegion(region);
		context.getInstance().setItems(getItems(player, region));
	}

	private static BiConsumer<Player, InventoryClickEvent> handleBanPlayer(Player player, Region region) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.players.ban")) {
				Messages.send(player, 8);
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
						if (MemberManager.isMemberOfRegion(region, targetPlayer)) MemberManager.removeMemberFromRegion(targetPlayer, region);
						if (InviteManager.isInvited(region, targetPlayer)) InviteManager.deleteInvitesOfPlayer(region, targetPlayer);

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

			if (!player.hasPermission("homestead.region.players.unban")) {
				Messages.send(player, 8);
				return;
			}
			if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
					RegionControlFlags.UNBAN_PLAYERS)) {
				return;
			}

			int bannedCount = BanManager.getBansOfRegion(region).size();
			if (bannedCount == 0) {
				Messages.send(player, 77);
				return;
			}

			BanManager.unbanAllPlayers(region);

			LogManager.addLog(region, player, LogManager.PredefinedLog.PURGE_BANS);

			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
			Messages.send(player, 94);

			Homestead.callEvent(new BulkUnbanPlayersEvent(region));

			Homestead.getInstance().runSyncTask(() -> new RegionBannedPlayers(player, region));
		};
	}

	private static boolean validateBan(Player player, Region region, String message) {
		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

		if (target == null) {
			Messages.send(player, 29, new Placeholder().add("{playername}", message));
			return false;
		}
		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.BAN_PLAYERS)) {
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
		SeRent rent = region.getRent();
		if (rent != null && rent.getRenterId().equals(target.getUniqueId())) {
			Messages.send(player, 196);
		}
		return true;
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