package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionTrustPlayerEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionUntrustPlayerEvent;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.BanManager;
import tfagaming.projects.minecraft.homestead.managers.InviteManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;

import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class RegionMembersMenu {
	private List<RegionMember> members;

	public RegionMembersMenu(Player player, Region region) {
		this.members = MemberManager.getMembersOfRegion(region);

		PaginationMenu.builder(5, 9 * 4)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(getItems(player, region))
				.fillEmptySlots()
				.goBack((_player, event) -> new RegionPlayersManagement(player, region))
				.onClick((_player, context) -> handleMemberClick(player, region, context))
				.actionButton(1, MenuUtility.getButton(29), handleTrustPlayer(player, region))
				.build()
				.open(player);
	}

	private void handleMemberClick(Player player, Region region, PaginationMenu.ClickContext context) {
		if (context.getIndex() >= members.size()) return;

		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		RegionMember member = members.get(context.getIndex());

		if (context.getEvent().isShiftClick() && context.getEvent().isRightClick()) {
			new PlayerInfo(player, member.getPlayer(), () -> new RegionMembersMenu(player, region));

		} else if (context.getEvent().isRightClick()) {
			new RegionMemberControlFlags(player, region, member);

		} else if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
			handleUntrust(player, region, member, context);

		} else if (context.getEvent().isLeftClick()) {
			new RegionMemberFlags(player, region, member);
		}
	}

	private void handleUntrust(Player player, Region region, RegionMember member, PaginationMenu.ClickContext context) {
		if (!MemberManager.isMemberOfRegion(region, member.getPlayer())) return;

		if (!player.hasPermission("homestead.region.players.untrust")) {
			Messages.send(player, 8);
			return;
		}
		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.UNTRUST_PLAYERS)) {
			return;
		}

		MemberManager.removeMemberFromRegion(member.getPlayer(), region);

		PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

		RegionUntrustPlayerEvent _event = new RegionUntrustPlayerEvent(region, player, member.getPlayer(),
				RegionUntrustPlayerEvent.UntrustReason.EXECUTION);
		Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

		members = MemberManager.getMembersOfRegion(region);
		context.getInstance().setItems(getItems(player, region));
	}

	private static BiConsumer<Player, InventoryClickEvent> handleTrustPlayer(Player player, Region region) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.players.trust")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			PlayerInputSession.builder(Homestead.getInstance(), player)
					.prompt(75)
					.validator(msg -> validateTrust(player, region, msg))
					.callback((p, input) -> {
						OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);
						if (targetPlayer == null) return;

						if (Resources.<RegionsFile>get(ResourceType.Regions).isInstantTrustSystemEnabled()) {
							InviteManager.deleteInvitesOfPlayer(region, targetPlayer);
							MemberManager.addMemberToRegion(targetPlayer, region);

							RegionTrustPlayerEvent _event = new RegionTrustPlayerEvent(region, player, targetPlayer);
							Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
						} else {
							InviteManager.invitePlayer(region, targetPlayer);
						}

						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
						Homestead.getInstance().runSyncTask(() -> new RegionMembersMenu(player, region));
					})
					.onCancel(p -> Homestead.getInstance().runSyncTask(() -> new RegionMembersMenu(player, region)))
					.build();
		};
	}

	private static boolean validateTrust(Player player, Region region, String message) {
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
		boolean taxesEnabled = Homestead.vault.isEconomyReady()
				&& Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("taxes.enabled");

		for (RegionMember member : members) {
			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", member.getPlayerName())
					.add("{member-joinedat}", Formatter.getDate(member.getJoinedAt()))
					.add("{taxes-dueon}", taxesEnabled && region.getTaxes() > 0
							? Formatter.getRemainingTime(member.getTaxesAt())
							: Formatter.getNever())
					.add("{tax-amount}", Formatter.getBalance(region.getTaxes()));

			items.add(MenuUtility.getButton(24, placeholder, member.getPlayer()));
		}

		return items;
	}
}