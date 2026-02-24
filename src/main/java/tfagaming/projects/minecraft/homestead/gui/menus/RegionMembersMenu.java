package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public class RegionMembersMenu {
	private List<SerializableMember> members;

	public RegionMembersMenu(Player player, Region region) {
		members = region.getMembers();

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(5), 9 * 4,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				getItems(player, region),
				(_player, event) -> new RegionPlayersManagement(player, region),
				(_player, context) -> {
					if (context.getIndex() >= members.size()) return;

					SerializableMember member = members.get(context.getIndex());

					if (context.getEvent().isShiftClick() && context.getEvent().isRightClick()) {
						new PlayerInfo(player, member.bukkit(), () -> new RegionMembersMenu(player, region));

					} else if (context.getEvent().isRightClick()) {
						if (!player.hasPermission("homestead.region.flags.members")) {
							Messages.send(player, 8);
							return;
						}
						new RegionMemberControlFlags(player, region, member);

					} else if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
						if (!region.isPlayerMember(member.bukkit())) return;

						if (!player.hasPermission("homestead.region.players.untrust")) {
							Messages.send(player, 8);
							return;
						}
						if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
								RegionControlFlags.UNTRUST_PLAYERS)) {
							return;
						}

						region.removeMember(member.bukkit());
						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
						RegionsManager.addNewLog(region.getUniqueId(), 3, new Placeholder()
								.add("{executor}", player.getName())
								.add("{playername}", member.bukkit() == null ? "?" : member.bukkit().getName()));

						members = region.getMembers();
						context.getInstance().setItems(getItems(player, region));

					} else if (context.getEvent().isLeftClick()) {
						if (!player.hasPermission("homestead.region.flags.members")) {
							Messages.send(player, 8);
							return;
						}
						new RegionMemberFlags(player, region, member);
					}
				});

		gui.addActionButton(1, MenuUtils.getButton(29), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.players.trust")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

				if (Homestead.config.isInstantTrustSystemEnabled()) {
					region.removePlayerInvite(targetPlayer);
					region.addMember(targetPlayer);
				} else {
					region.addPlayerInvite(targetPlayer);
					RegionsManager.addNewLog(region.getUniqueId(), 2, new Placeholder()
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
				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.TRUST_PLAYERS)) {
					return false;
				}
				if (region.isPlayerBanned(target)) {
					Messages.send(player, 74);
					return false;
				}
				if (region.isPlayerMember(target)) {
					Messages.send(player, 48, new Placeholder().add("{playername}", target.getName()));
					return false;
				}
				if (region.isPlayerInvited(target)) {
					Messages.send(player, 35, new Placeholder().add("{playername}", target.getName()));
					return false;
				}
				if (region.isOwner(target)) {
					Messages.send(player, 30);
					return false;
				}

				SerializableRent rent = region.getRent();
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

		gui.open(player, MenuUtils.getEmptySlot());
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();
		boolean taxesEnabled = Homestead.vault.isEconomyReady() && Homestead.config.getBoolean("taxes.enabled");

		for (SerializableMember member : members) {
			OfflinePlayer memberBukkit = member.bukkit();

			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", memberBukkit == null ? "?" : memberBukkit.getName())
					.add("{member-joinedat}", Formatter.getDate(member.getJoinedAt()))
					.add("{taxes-dueon}", taxesEnabled && region.getTaxesAmount() > 0
							? Formatter.getRemainingTime(member.getTaxesAt())
							: Formatter.getNever())
					.add("{tax-amount}", Formatter.getBalance(region.getTaxesAmount()));

			items.add(MenuUtils.getButton(24, placeholder, member.bukkit()));
		}

		return items;
	}
}