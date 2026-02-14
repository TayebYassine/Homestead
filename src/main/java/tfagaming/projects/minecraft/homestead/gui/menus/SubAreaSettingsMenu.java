package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.sessions.playerinput.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;

public class SubAreaSettingsMenu {
	public SubAreaSettingsMenu(Player player, Region region, SubArea subArea) {
		Menu gui = new Menu(MenuUtils.getTitle(15).replace("{subarea}", subArea.getName()), 9 * 3);

		boolean isEconomyEnabled = Homestead.vault.isEconomyReady();
		boolean isRentEnabled = isEconomyEnabled && Homestead.config.getBoolean("renting.enabled");

		HashMap<String, String> replacements = new HashMap<>();
		replacements.put("{subarea}", subArea.getName());

		ItemStack renameSubAreaButton = MenuUtils.getButton(43, replacements);

		gui.addItem(11, renameSubAreaButton, (_player, event) -> {
			if (!event.isLeftClick()) {
				return;
			}

			if (!player.hasPermission("homestead.region.subareas.rename")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				subArea.setName(input);

				Homestead.getInstance().runSyncTask(() -> {
					new SubAreaSettingsMenu(player, region, subArea);
				});
			}, (message) -> {
				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.MANAGE_SUBAREAS)) {
					return false;
				}

				if (!StringUtils.isValidSubAreaName(message)) {
					Messages.send(player, 57);
					return false;
				}

				if (subArea.getName().equalsIgnoreCase(message)) {
					Messages.send(player, 11);
					return false;
				}

				if (SubAreasManager.isNameUsed(region.getUniqueId(), message)) {
					Messages.send(player, 58);
					return false;
				}

				return true;
			}, (__player) -> {
				Homestead.getInstance().runSyncTask(() -> {
					new SubAreaSettingsMenu(player, region, subArea);
				});
			}, 88);
		});

		ItemStack flagsSubAreabutton = MenuUtils.getButton(44, replacements);

		gui.addItem(12, flagsSubAreabutton, (_player, event) -> {
			if (!event.isLeftClick()) {
				return;
			}

			if (!player.hasPermission("homestead.region.subareas.flags")) {
				Messages.send(player, 8);
				return;
			}

			new SubAreaFlagsMenu(player, region, subArea);
		});

		replacements.put("{subarea-players}", String.valueOf(subArea.getMembers().size()));

		ItemStack membersSubAreabutton = MenuUtils.getButton(70, replacements);

		gui.addItem(13, membersSubAreabutton, (_player, event) -> {
			if (!event.isLeftClick()) {
				return;
			}

			if (!player.hasPermission("homestead.region.subareas.flags")) {
				Messages.send(player, 8);
				return;
			}

			new SubAreaMembersMenu(player, region, subArea);
		});

		SerializableRent rent = subArea.getRent();

		if (rent != null) {
			replacements.put("{rent-enabled}", Formatters.getEnabled(isRentEnabled));
			replacements.put("{rent-renter}", rent.getPlayer().getName());
			replacements.put("{rent-price}", Formatters.getBalance(rent.getPrice()));
			replacements.put("{rent-until}", Formatters.formatRemainingTime(rent.getUntilAt()));
		} else {
			replacements.put("{rent-enabled}", Formatters.getEnabled(isRentEnabled));
			replacements.put("{rent-renter}", Formatters.getNone());
			replacements.put("{rent-price}", Formatters.getNone());
			replacements.put("{rent-until}", Formatters.getNone());
		}

		ItemStack rentButton = MenuUtils.getButton(71, replacements);

		gui.addItem(14, rentButton, (_player, event) -> {
			if (event.isLeftClick()) {
				boolean isOwnerOrOperator = PlayerUtils.isOperator(player) || region.isOwner(player);
				if (!isOwnerOrOperator) {
					Messages.send(player, 159);
					return;
				}

				if (subArea.getRent() == null) {
					Messages.send(player, 195);
				} else {
					subArea.setRent(null);

					Messages.send(player, 127);

					new SubAreaSettingsMenu(player, region, subArea);
				}
			}
		});

		ItemStack deleteSubAreaButton = MenuUtils.getButton(45, replacements);

		gui.addItem(15, deleteSubAreaButton, (_player, event) -> {
			if (!event.isLeftClick()) {
				return;
			}

			if (!player.hasPermission("homestead.region.subareas.delete")) {
				Messages.send(player, 8);
				return;
			}

			if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
					RegionControlFlags.MANAGE_SUBAREAS)) {
				return;
			}

			SubAreasManager.deleteSubArea(subArea.getUniqueId());

			new SubAreasMenu(player, region);
		});

		gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
			if (!event.isLeftClick()) {
				return;
			}

			new SubAreasMenu(player, region);
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}
}
