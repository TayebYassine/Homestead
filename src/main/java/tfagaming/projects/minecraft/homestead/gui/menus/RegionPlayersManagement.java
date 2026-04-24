package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;

public final class RegionPlayersManagement {
	public RegionPlayersManagement(Player player, Region region) {
		Menu gui = new Menu(MenuUtility.getTitle(4), 9 * 3);

		Placeholder placeholder = new Placeholder()
				.add("{region-members}", region.getMembers().size())
				.add("{region-members-max}", Limits.getRegionLimit(region, Limits.LimitType.MEMBERS_PER_REGION))
				.add("{region-banned-players}", region.getBannedPlayers().size())
				.add("{region-invited-players}", region.getInvitedPlayers().size());

		gui.addItem(11, MenuUtility.getButton(18, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new RegionMembersMenu(player, region);
		});

		gui.addItem(13, MenuUtility.getButton(19, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new RegionBannedPlayers(player, region);
		});

		gui.addItem(15, MenuUtility.getButton(20, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new RegionPlayersInvited(player, region);
		});

		gui.addItem(18, MenuUtility.getBackButton(), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new RegionMenu(player, region);
		});

		gui.open(player, MenuUtility.getEmptySlot());
	}
}