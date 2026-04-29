package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionTrustPlayerEvent;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionInvite;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;

import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class RegionPlayersInvited {
	private List<RegionInvite> invites;

	public RegionPlayersInvited(Player player, Region region) {
		this.invites = InviteManager.getInvitesOfRegion(region);

		PaginationMenu.builder(10, 9 * 4)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(getItems(player, region))
				.fillEmptySlots()
				.goBack((_player, event) -> new RegionPlayersManagement(player, region))
				.onClick((_player, context) -> handleInviteClick(player, region, context))
				.actionButton(0, MenuUtility.getButton(29), handleInvitePlayer(player, region))
				.actionButton(2, MenuUtility.getButton(31), handleClearInvites(player, region))
				.build()
				.open(player);
	}

	private void handleInviteClick(Player player, Region region, PaginationMenu.ClickContext context) {
		if (context.getIndex() >= invites.size()) return;

		RegionInvite invite = invites.get(context.getIndex());
		if (!context.getEvent().isLeftClick()) return;
		if (!InviteManager.isInvited(region, invite.getPlayer())) return;

		InviteManager.deleteInvite(invite.getUniqueId());
		invites = InviteManager.getInvitesOfRegion(region);
		context.getInstance().setItems(getItems(player, region));
	}

	private static BiConsumer<Player, InventoryClickEvent> handleInvitePlayer(Player player, Region region) {
		return (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}
			if (!event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.players.trust")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			PlayerInputSession.builder(Homestead.getInstance(), player)
					.prompt(75)
					.validator(msg -> validateInvite(player, region, msg))
					.callback((p, input) -> {
						OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);
						if (targetPlayer == null) return;

						if (Resources.<RegionsFile>get(ResourceType.Regions).isInstantTrustSystemEnabled()) {
							InviteManager.deleteInvitesOfPlayer(region, targetPlayer);
							MemberManager.addMemberToRegion(targetPlayer, region);

							LogManager.addLog(region, targetPlayer, LogManager.PredefinedLog.JOIN_REGION);

							RegionTrustPlayerEvent _event = new RegionTrustPlayerEvent(region, player, targetPlayer);
							Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
						} else {
							InviteManager.invitePlayer(region, targetPlayer);

							Placeholder placeholder = new Placeholder()
									.add("{region}", region.getName())
									.add("{playername}", targetPlayer.getName())
									.add("{ownername}", region.getOwnerName());

							Messages.send(player, 36, placeholder);

							if (targetPlayer.isOnline()) {
								Messages.send(targetPlayer.getPlayer(), 139, placeholder);
							}

							LogManager.addLog(region, player, LogManager.PredefinedLog.INVITE_PLAYER, targetPlayer.getName());
						}

						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
						Homestead.getInstance().runSyncTask(() -> new RegionPlayersInvited(player, region));
					})
					.onCancel(p -> Homestead.getInstance().runSyncTask(() -> new RegionPlayersInvited(player, region)))
					.build();
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleClearInvites(Player player, Region region) {
		return (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}
			if (!event.isLeftClick()) return;

			if (InviteManager.getInvitesOfRegion(region).isEmpty()) {
				Messages.send(player, 76);
				return;
			}

			InviteManager.deleteInvitesOfRegion(region);

			LogManager.addLog(region, player, LogManager.PredefinedLog.PURGE_INVITES);

			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
			Messages.send(player, 95);
			Homestead.getInstance().runSyncTask(() -> new RegionPlayersInvited(player, region));
		};
	}

	private static boolean validateInvite(Player player, Region region, String message) {
		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

		if (target == null) {
			Messages.send(player, 29, new Placeholder().add("{playername}", message));
			return false;
		}
		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.TRUST_PLAYERS)) {
			return false;
		}
		if (BanManager.isBanned(region, target)) {
			Messages.send(player, 74);
			return false;
		}
		if (MemberManager.isMemberOfRegion(region, target)) {
			Messages.send(player, 48, new Placeholder().add("{playername}", target.getName()));
			return false;
		}
		if (InviteManager.isInvited(region, target)) {
			Messages.send(player, 35, new Placeholder().add("{playername}", target.getName()));
			return false;
		}
		if (region.isOwner(target)) {
			Messages.send(player, 30);
			return false;
		}

		SeRent rent = region.getRent();
		if (rent != null && rent.getRenterId().equals(target.getUniqueId())) {
			Messages.send(player, 196);
			return false;
		}
		if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
			Messages.send(player, 116);
			return false;
		}
		return true;
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (RegionInvite invite : invites) {
			items.add(MenuUtility.getButton(30, new Placeholder()
					.add("{playername}", invite.getPlayerName())));
		}

		return items;
	}
}