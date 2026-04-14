package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public final class SubAreaMenu {
	public SubAreaMenu(Player player, Region region, SubArea subArea) {
		Menu gui = new Menu(MenuUtils.getTitle(15).replace("{subarea}", subArea.getName()), 9 * 3);

		boolean isEconomyEnabled = Homestead.vault.isEconomyReady();
		boolean isRentEnabled = isEconomyEnabled && Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("renting.enabled");

		SerializableRent rent = subArea.getRent();

		Placeholder placeholder = new Placeholder()
				.add("{subarea}", subArea.getName())
				.add("{subarea-players}", subArea.getMembers().size())
				.add("{rent-enabled}", Formatter.getToggle(isRentEnabled))
				.add("{rent-renter}", rent != null ? rent.getPlayer().getName() : Formatter.getNone())
				.add("{rent-price}", rent != null ? Formatter.getBalance(rent.getPrice()) : Formatter.getNone())
				.add("{rent-until}", rent != null ? Formatter.getRemainingTime(rent.getUntilAt()) : Formatter.getNever());

		gui.addItem(11, MenuUtils.getButton(43, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.subareas.rename")) {
				Messages.send(player, 8);
				return;
			}

			player.closeInventory();

			new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
				subArea.setName(input);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				Homestead.getInstance().runSyncTask(() -> new SubAreaMenu(player, region, subArea));
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
				if (SubAreaManager.isNameUsed(region.getUniqueId(), message)) {
					Messages.send(player, 58);
					return false;
				}
				if (ColorTranslator.containsMiniMessageTag(message)) {
					Messages.send(player, 30);
					return false;
				}
				return true;
			}, (__player) -> Homestead.getInstance().runSyncTask(() -> new SubAreaMenu(player, region, subArea)), 88);
		});

		gui.addItem(12, MenuUtils.getButton(44, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			new SubAreaFlagsMenu(player, region, subArea);
		});

		gui.addItem(13, MenuUtils.getButton(70, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			new SubAreaMembers(player, region, subArea);
		});

		gui.addItem(14, MenuUtils.getButton(71, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!PlayerUtils.isOperator(player) && !region.isOwner(player)) {
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
		});

		gui.addItem(15, MenuUtils.getButton(45, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!player.hasPermission("homestead.region.subareas.delete")) {
				Messages.send(player, 8);
				return;
			}
			if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
					RegionControlFlags.MANAGE_SUBAREAS)) {
				return;
			}

			SubAreaManager.deleteSubArea(subArea.getUniqueId());
			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
			new SubAreasMenu(player, region);
		});

		gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new SubAreasMenu(player, region);
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}
}