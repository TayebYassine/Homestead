package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionTrustPlayerEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionUntrustPlayerEvent;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
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

public final class RegionMembersMenu {
	private List<RegionMember> members;

	public RegionMembersMenu(Player player, Region region) {
		members = MemberManager.getMembersOfRegion(region);

		PaginationMenu gui = new PaginationMenu(
				MenuUtility.getTitle(5), 9 * 4,
				MenuUtility.getNextPageButton(),
				MenuUtility.getPreviousPageButton(),
				getItems(player, region),
				(_player, event) -> new RegionPlayersManagement(player, region),
				(_player, context) -> {
					if (context.getIndex() >= members.size()) return;

					if (RegionManager.findRegion(region.getUniqueId()) == null) {
						player.closeInventory();
						return;
					}

					RegionMember member = members.get(context.getIndex());

					if (context.getEvent().isShiftClick() && context.getEvent().isRightClick()) {
						new PlayerInfo(player, member.bukkit(), () -> new RegionMembersMenu(player, region));

					} else if (context.getEvent().isRightClick()) {
						new RegionMemberControlFlags(player, region, member);

					} else if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
						if (!MemberManager.isMemberOfRegion(region, member.bukkit())) return;

						if (!player.hasPermission("homestead.region.players.untrust")) {
							Messages.send(player, 8);
							return;
						}
						if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
								RegionControlFlags.UNTRUST_PLAYERS)) {
							return;
						}

						region.removeMember(member.bukkit());
						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
						RegionManager.addNewLog(region.getUniqueId(), 3, new Placeholder()
								.add("{executor}", player.getName())
								.add("{playername}", member.bukkit() == null ? "?" : member.bukkit().getName()));

						RegionUntrustPlayerEvent _event = new RegionUntrustPlayerEvent(region, player, member.bukkit(), RegionUntrustPlayerEvent.UntrustReason.EXECUTION);
						Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

						members = MemberManager.getMembersOfRegion(region);
						context.getInstance().setItems(getItems(player, region));

					} else if (context.getEvent().isLeftClick()) {
						new RegionMemberFlags(player, region, member);
					}
				});

		gui.addActionButton(1, MenuUtility.getButton(29), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.players.trust")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

				if (Resources.<RegionsFile>get(ResourceType.Regions).isInstantTrustSystemEnabled()) {
					InviteManager.deleteInvitesOfPlayer(region, targetPlayer);
					InviteManager.deleteInvitesOfPlayer(region, targetPlayer);

					RegionTrustPlayerEvent _event = new RegionTrustPlayerEvent(region, player, targetPlayer);
					Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
				} else {
					region.addPlayerInvite(targetPlayer);
					RegionManager.addNewLog(region.getUniqueId(), 2, new Placeholder()
							.add("{executor}", player.getName())
							.add("{playername}", targetPlayer.getName()));
				}

				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				Homestead.getInstance().runSyncTask(() -> new RegionMembersMenu(player, region));
			}, (message) -> {
				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

				if (target == null) {
					Messages.send(player, 29, new Placeholder().add("{playername}", message));
					return false;
				}
				if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.TRUST_PLAYERS)) {
					return false;
				}
				if (BannedPlayerManager.isBanned(region, target)) {
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
				if (rent != null && rent.getPlayerId().equals(target.getUniqueId())) {
					Messages.send(player, 196);
					return false;
				}
				if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
					Messages.send(player, 116);
					return false;
				}
				return true;
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new RegionMembersMenu(player, region)), 75);
		});

		gui.open(player, MenuUtility.getEmptySlot());
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();
		boolean taxesEnabled = Homestead.vault.isEconomyReady() && Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("taxes.enabled");

		for (RegionMember member : members) {
			OfflinePlayer memberBukkit = member.bukkit();

			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", memberBukkit == null ? "?" : memberBukkit.getName())
					.add("{member-joinedat}", Formatter.getDate(member.getJoinedAt()))
					.add("{taxes-dueon}", taxesEnabled && region.getTaxesAmount() > 0
							? Formatter.getRemainingTime(member.getTaxesAt())
							: Formatter.getNever())
					.add("{tax-amount}", Formatter.getBalance(region.getTaxesAmount()));

			items.add(MenuUtility.getButton(24, placeholder, member.bukkit()));
		}

		return items;
	}
}