package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.models.SubArea;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class SubAreaMembers {
	private List<RegionMember> members;

	public SubAreaMembers(Player player, Region region, SubArea subArea) {
		this.members = MemberManager.getMembersOfSubArea(subArea);

		PaginationMenu.builder(24, 9 * 4)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(getItems(player, region, subArea))
				.fillEmptySlots()
				.goBack((_player, event) -> new SubAreaMenu(player, region, subArea))
				.onClick((_player, context) -> handleMemberClick(player, region, subArea, context))
				.actionButton(1, MenuUtility.getButton(68), handleAddMember(player, region, subArea))
				.build()
				.open(player);
	}

	private static BiConsumer<Player, InventoryClickEvent> handleAddMember(Player player, Region region, SubArea subArea) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.subareas.players")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			PlayerInputSession.builder(Homestead.getInstance(), player)
					.prompt(75)
					.validator(msg -> validateAddMember(player, region, subArea, msg))
					.callback((p, input) -> {
						OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);
						MemberManager.addMemberToSubArea(targetPlayer, subArea);

						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
						Homestead.getInstance().runSyncTask(() -> new SubAreaMembers(player, region, subArea));
					})
					.onCancel(p -> Homestead.getInstance().runSyncTask(() -> new SubAreaMembers(player, region, subArea)))
					.build();
		};
	}

	private static boolean validateAddMember(Player player, Region region, SubArea subArea, String message) {
		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

		if (target == null) {
			Messages.send(player, 29, new Placeholder().add("{playername}", message));
			return false;
		}
		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.MANAGE_SUBAREAS)) {
			return false;
		}
		if (region.isOwner(target)) {
			Messages.send(player, 30);
			return false;
		}
		if (!MemberManager.isMemberOfRegion(region, target)) {
			Messages.send(player, 171);
			return false;
		}
		if (MemberManager.isMemberOfSubArea(subArea, target)) {
			Messages.send(player, 174);
			return false;
		}
		return true;
	}

	private void handleMemberClick(Player player, Region region, SubArea subArea, PaginationMenu.ClickContext context) {
		if (context.getIndex() >= members.size()) return;

		if (RegionManager.findRegion(region.getUniqueId()) == null || SubAreaManager.findSubArea(subArea.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		if (!player.hasPermission("homestead.region.subareas.players.flags")) {
			Messages.send(player, 8);
			return;
		}

		RegionMember member = members.get(context.getIndex());

		if (context.getEvent().isShiftClick() && context.getEvent().isRightClick()) {
			new PlayerInfo(player, member.getPlayer(), () -> new SubAreaMembers(player, region, subArea));

		} else if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
			handleRemoveMember(player, region, subArea, member, context);

		} else if (context.getEvent().isLeftClick()) {
			new SubAreaMemberFlags(player, region, subArea, member);
		}
	}

	private void handleRemoveMember(Player player, Region region, SubArea subArea, RegionMember member, PaginationMenu.ClickContext context) {
		if (!MemberManager.isMemberOfRegion(region.getUniqueId(), member.getPlayerId())
				|| !MemberManager.isMemberOfSubArea(subArea, player)) {
			return;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.MANAGE_SUBAREAS)) {
			return;
		}

		MemberManager.removeMemberFromSubArea(player, subArea);

		PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

		members = MemberManager.getMembersOfSubArea(subArea);
		context.getInstance().setItems(getItems(player, region, subArea));
	}

	private List<ItemStack> getItems(Player player, Region region, SubArea subArea) {
		List<ItemStack> items = new ArrayList<>();

		for (RegionMember member : members) {
			items.add(MenuUtility.getButton(69, new Placeholder()
							.add("{region}", region.getName())
							.add("{subarea}", subArea.getName())
							.add("{playername}", member.getPlayerName()),
					member.getPlayer()));
		}

		return items;
	}
}