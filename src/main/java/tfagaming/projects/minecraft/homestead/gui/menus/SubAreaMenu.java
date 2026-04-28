package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.SubArea;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.function.BiConsumer;

public final class SubAreaMenu {
	public SubAreaMenu(Player player, Region region, SubArea subArea) {
		boolean isEconomyEnabled = Homestead.vault.isEconomyReady();
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

			if (!player.hasPermission("homestead.region.subareas.rename")) {
				Messages.send(player, 8);
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
				Messages.send(player, 159);
				return;
			}

			if (subArea.getRent() == null) {
				Messages.send(player, 195);
			} else {
				subArea.setRent(null);
				Messages.send(player, 127);
				new SubAreaMenu(player, region, subArea);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleDelete(Player player, Region region, SubArea subArea) {
		return (_player, event) -> {
			if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.subareas.delete")) {
				Messages.send(player, 8);
				return;
			}
			if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
					RegionControlFlags.MANAGE_SUBAREAS)) {
				return;
			}

			SubAreaManager.deleteSubArea(subArea.getUniqueId());
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
		if (SubAreaManager.isNameUsed(region.getUniqueId(), message)) {
			Messages.send(player, 58);
			return false;
		}
		if (ColorTranslator.containsMiniMessageTag(message)) {
			Messages.send(player, 30);
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