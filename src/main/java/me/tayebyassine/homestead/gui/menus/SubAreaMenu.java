package me.tayebyassine.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.PlayerInputSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.java.StringUtils;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.menus.MenuUtility;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;

import java.util.function.BiConsumer;

public final class SubAreaMenu {
	public SubAreaMenu(Player player, Region region, SubArea subArea) {
		boolean isEconomyEnabled = Homestead.VAULT.isEconomyReady();
		boolean isRentEnabled = isEconomyEnabled && Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("renting.enabled");

		SeRent rent = subArea.getRent();

		Placeholder placeholder = new Placeholder()
				.add("{subarea}", subArea.getName())
				.add("{subarea-players}", MemberManager.getMembersOfRegion(region).size())
				.add("{rent-enabled}", Formatter.getToggle(isRentEnabled))
				.add("{rent-renter}", rent != null ? rent.getRenterName() : Formatter.getNone())
				.add("{rent-price}", rent != null ? Formatter.getBalance(rent.getPrice()) : Formatter.getNone())
				.add("{rent-until}", rent != null ? Formatter.getRemainingTime(rent.getUntilAt()) : Formatter.getNever());

		Menu.builder(MenuUtility.getTitle(15).replace("{subarea}", subArea.getName()), 9 * 3)
				.button(11, MenuUtility.getButton(43, placeholder), handleRename(player, region, subArea))
				.button(12, MenuUtility.getButton(44, placeholder), handleFlags(player, region, subArea))
				.button(13, MenuUtility.getButton(70, placeholder), handleMembers(player, region, subArea))
				.button(14, MenuUtility.getButton(71, placeholder), handleEndRent(player, region, subArea))
				.button(15, MenuUtility.getButton(45, placeholder), handleDelete(player, region, subArea))
				.button(18, MenuUtility.getBackButton(), handleBack(player, region, subArea))
				.fillEmptySlots()
				.build()
				.open(player);
	}

	private static BiConsumer<Player, InventoryClickEvent> handleRename(Player player, Region region, SubArea subArea) {
		return (_player, event) -> {
			if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!player.hasPermission("homestead.actions.regions.subareas.update.name")) {
				Messages.send(player, "common.no_permission");
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}

			player.closeInventory();

			PlayerInputSession.builder(Homestead.getInstance(), player)
					.prompt(88)
					.validator(msg -> validateRename(player, region, subArea, msg))
					.callback((p, input) -> {
						subArea.setName(input);
						PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
						Homestead.getInstance().runSyncTask(() -> new SubAreaMenu(player, region, subArea));
					})
					.onCancel(p -> Homestead.getInstance().runSyncTask(() -> new SubAreaMenu(player, region, subArea)))
					.build();
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleFlags(Player player, Region region, SubArea subArea) {
		return (_player, event) -> {
			if (checkValid(player, region, subArea) && event.isLeftClick()) {
				new SubAreaFlagsMenu(player, region, subArea);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleMembers(Player player, Region region, SubArea subArea) {
		return (_player, event) -> {
			if (checkValid(player, region, subArea) && event.isLeftClick()) {
				new SubAreaMembers(player, region, subArea);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleEndRent(Player player, Region region, SubArea subArea) {
		return (_player, event) -> {
			if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

			if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
				Messages.send(player, "common.no_permission");
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}

			if (subArea.getRent() == null) {
				Messages.send(player, "commands.rent.0");
			} else {
				subArea.setRent(null);

				new SubAreaMenu(player, region, subArea);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleDelete(Player player, Region region, SubArea subArea) {
		return (_player, event) -> {
			if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

			if (!player.hasPermission("homestead.actions.regions.subareas.delete")) {
				Messages.send(player, "common.no_permission");
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}
			if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
					ControlFlag.MANAGE_SUBAREAS.getBitmask())) {
				return;
			}

			SubAreaManager.deleteSubArea(subArea.getUniqueId());

			LogManager.addLog(region, player, LogManager.PredefinedLog.DELETE_SUBAREA);

			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

			new SubAreasMenu(player, region);
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleBack(Player player, Region region, SubArea subArea) {
		return (_player, event) -> {
			if (checkValid(player, region, subArea) && event.isLeftClick()) {
				new SubAreasMenu(player, region);
			}
		};
	}

	private static boolean validateRename(Player player, Region region, SubArea subArea, String message) {
		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlag.MANAGE_SUBAREAS.getBitmask())) {
			return false;
		}
		if (!StringUtils.isValidSubAreaName(message)) {
			Messages.send(player, "commands.subareas.5");
			return false;
		}
		if (subArea.getName().equalsIgnoreCase(message)) {
			Messages.send(player, "commands.subareas.12");
			return false;
		}
		if (SubAreaManager.isNameUsed(region.getUniqueId(), message)) {
			Messages.send(player, "commands.subareas.6");
			return false;
		}
		if (ColorTranslator.containsMiniMessageTag(message)) {
			Messages.send(player, "commands.subareas.13");
			return false;
		}
		return true;
	}

	private static boolean checkValid(Player player, Region region, SubArea subArea) {
		if (RegionManager.findRegion(region.getUniqueId()) == null || SubAreaManager.findSubArea(subArea.getUniqueId()) == null) {
			player.closeInventory();
			return false;
		}
		return true;
	}
}
