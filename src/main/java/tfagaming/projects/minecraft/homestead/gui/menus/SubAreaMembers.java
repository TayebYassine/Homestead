package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public final class SubAreaMembers {
	private List<SerializableMember> members;

	public SubAreaMembers(Player player, Region region, SubArea subArea) {
		members = subArea.getMembers();

		PaginationMenu gui = new PaginationMenu(
				MenuUtility.getTitle(24), 9 * 4,
				MenuUtility.getNextPageButton(),
				MenuUtility.getPreviousPageButton(),
				getItems(player, region, subArea),
				(_player, event) -> new SubAreaMenu(player, region, subArea),
				(_player, context) -> {
					if (context.getIndex() >= members.size()) return;

					if (RegionManager.findRegion(region.getUniqueId()) == null || SubAreaManager.findSubArea(subArea.getUniqueId()) == null) {
						player.closeInventory();
						return;
					}

					if (!player.hasPermission("homestead.region.subareas.players.flags")) {
						Messages.send(player, 8);
						return;
					}

					SerializableMember member = members.get(context.getIndex());

					if (context.getEvent().isShiftClick() && context.getEvent().isRightClick()) {
						new PlayerInfo(player, member.bukkit(), () ->
								new SubAreaMembers(player, region, subArea));

					} else if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
						if (!region.isPlayerMember(member.bukkit())
								|| !subArea.isPlayerMember(member.bukkit())) {
							return;
						}

						if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
								RegionControlFlags.MANAGE_SUBAREAS)) {
							return;
						}

						subArea.removeMember(member.bukkit());
						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

						members = subArea.getMembers();
						context.getInstance().setItems(getItems(player, region, subArea));

					} else if (context.getEvent().isLeftClick()) {
						new SubAreaMemberFlags(player, region, subArea, member);
					}
				});

		gui.addActionButton(1, MenuUtility.getButton(68), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.subareas.players")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

				subArea.addMember(targetPlayer);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				Homestead.getInstance().runSyncTask(() -> new SubAreaMembers(player, region, subArea));
			}, (message) -> {
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
				if (!region.isPlayerMember(target)) {
					Messages.send(player, 171);
					return false;
				}
				if (subArea.isPlayerMember(target)) {
					Messages.send(player, 174);
					return false;
				}
				return true;
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new SubAreaMembers(player, region, subArea)), 75);
		});

		gui.open(player, MenuUtility.getEmptySlot());
	}

	private List<ItemStack> getItems(Player player, Region region, SubArea subArea) {
		List<ItemStack> items = new ArrayList<>();

		for (SerializableMember member : members) {
			OfflinePlayer memberBukkit = member.bukkit();

			items.add(MenuUtility.getButton(69, new Placeholder()
							.add("{region}", region.getName())
							.add("{subarea}", subArea.getName())
							.add("{playername}", memberBukkit == null ? "?" : memberBukkit.getName()),
					member.bukkit()));
		}

		return items;
	}
}