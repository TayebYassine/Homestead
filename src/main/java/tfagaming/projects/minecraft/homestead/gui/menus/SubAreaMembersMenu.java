package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.sessions.playerinput.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubAreaMembersMenu {
	List<SerializableMember> members;

	public SubAreaMembersMenu(Player player, Region region, SubArea subArea) {
		members = subArea.getMembers();

		PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(24), 9 * 4,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(), getItems(player, region, subArea), (_player, event) -> {
			new SubAreaSettingsMenu(player, region, subArea);
		}, (_player, context) -> {
			if (context.getIndex() >= members.size()) {
				return;
			}

			SerializableMember member = members.get(context.getIndex());

			if (context.getEvent().isShiftClick() && context.getEvent().isRightClick()) {
				new PlayerInfoMenu(player, member.getBukkitOfflinePlayer(), () -> {
					new SubAreaMembersMenu(player, region, subArea);
				});
			} else if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
				if (region.isPlayerMember(member.getBukkitOfflinePlayer()) && subArea.isPlayerMember(member.getBukkitOfflinePlayer())) {
					if (!player.hasPermission("homestead.region.subareas.players")) {
						Messages.send(player, 8);
						return;
					}

					if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
							RegionControlFlags.MANAGE_SUBAREAS)) {
						return;
					}

					subArea.removeMember(member.getBukkitOfflinePlayer());

					PaginationMenu instance = context.getInstance();

					members = subArea.getMembers();

					instance.setItems(getItems(player, region, subArea));
				}
			} else if (context.getEvent().isLeftClick()) {
				if (!player.hasPermission("homestead.region.subareas.players")) {
					Messages.send(player, 8);
					return;
				}

				new SubAreaMemberPlayerFlagsMenu(player, region, subArea, member);
			}
		});

		gui.addActionButton(1, MenuUtils.getButton(68), (_player, event) -> {
			if (!event.isLeftClick()) {
				return;
			}

			if (!player.hasPermission("homestead.region.subareas.players")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

				subArea.addMember(targetPlayer);

				Homestead.getInstance().runSyncTask(() -> {
					new SubAreaMembersMenu(player, region, subArea);
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
			}, (__player) -> {
				Homestead.getInstance().runSyncTask(() -> {
					new SubAreaMembersMenu(player, region, subArea);
				});
			}, 75);
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	public List<ItemStack> getItems(Player player, Region region, SubArea subArea) {
		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < members.size(); i++) {
			SerializableMember member = members.get(i);

			HashMap<String, String> replacements = new HashMap<>();

			replacements.put("{region}", region.getName());
			replacements.put("{subarea}", subArea.getName());
			replacements.put("{playername}", member.getBukkitOfflinePlayer().getName());

			items.add(MenuUtils.getButton(69, replacements, member.getBukkitOfflinePlayer()));
		}

		return items;
	}
}
