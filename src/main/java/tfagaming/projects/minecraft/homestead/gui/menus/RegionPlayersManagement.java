package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.BanManager;
import tfagaming.projects.minecraft.homestead.managers.InviteManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;

import java.util.function.BiConsumer;

public final class RegionPlayersManagement {
	public RegionPlayersManagement(Player player, Region region) {
		Placeholder placeholder = new Placeholder()
				.add("{region-members}", MemberManager.getMembersOfRegion(region).size())
				.add("{region-members-max}", Limits.getRegionLimit(region, Limits.LimitType.MEMBERS_PER_REGION))
				.add("{region-banned-players}", BanManager.getBansOfRegion(region).size())
				.add("{region-invited-players}", InviteManager.getInvitesOfRegion(region).size());

		Menu.builder(4, 9 * 3)
				.button(11, MenuUtility.getButton(18, placeholder), handleMembers(player, region))
				.button(13, MenuUtility.getButton(19, placeholder), handleBanned(player, region))
				.button(15, MenuUtility.getButton(20, placeholder), handleInvited(player, region))
				.button(18, MenuUtility.getBackButton(), handleBack(player, region))
				.fillEmptySlots()
				.build()
				.open(player);
	}

	private static BiConsumer<Player, InventoryClickEvent> handleMembers(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegion(player, region) && event.isLeftClick()) {
				new RegionMembersMenu(player, region);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleBanned(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegion(player, region) && event.isLeftClick()) {
				new RegionBannedPlayers(player, region);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleInvited(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegion(player, region) && event.isLeftClick()) {
				new RegionPlayersInvited(player, region);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleBack(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegion(player, region) && event.isLeftClick()) {
				new RegionMenu(player, region);
			}
		};
	}

	private static boolean checkRegion(Player player, Region region) {
		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return false;
		}
		return true;
	}
}