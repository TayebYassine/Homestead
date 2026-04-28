package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RateManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager.RegionSorting;

import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;

import java.util.function.BiConsumer;

public final class RegionInfoMenu {
	public RegionInfoMenu(Player player, Region region, Runnable backButton) {
		Placeholder placeholder = new Placeholder()
				.add("{region}", region.getName())
				.add("{region-createdat}", Formatter.getDate(region.getCreatedAt()))
				.add("{region-members}", Formatter.getMembersOfRegion(region))
				.add("{region-bank}", Formatter.getBalance(region.getBank()))
				.add("{region-rating}", Formatter.getRating(RateManager.getAverageRating(region)))
				.add("{region-owner}", region.getOwnerName())
				.add("{region-global-rank}", RegionManager.getGlobalRank(region.getUniqueId()))
				.add("{region-rank-bank}", RegionManager.getRank(RegionSorting.BANK, region.getUniqueId()))
				.add("{region-rank-chunks}", RegionManager.getRank(RegionSorting.CHUNKS_COUNT, region.getUniqueId()))
				.add("{region-rank-members}", RegionManager.getRank(RegionSorting.MEMBERS_COUNT, region.getUniqueId()))
				.add("{region-rank-rating}", RegionManager.getRank(RegionSorting.RATING, region.getUniqueId()));

		Menu.builder(MenuUtility.getTitle(8).replace("{region}", region.getName()), 9 * 3)
				.item(11, MenuUtility.getButton(25, placeholder))
				.item(13, MenuUtility.getButton(26, placeholder))
				.button(15, MenuUtility.getButton(61, placeholder), handleRating(player, region, backButton))
				.button(18, MenuUtility.getBackButton(), handleBack(player, region, backButton))
				.fillEmptySlots()
				.build()
				.open(player);
	}

	private static BiConsumer<Player, InventoryClickEvent> handleRating(Player player, Region region, Runnable backButton) {
		return (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}
			if (!event.isLeftClick()) return;

			new RegionRating(player, region, () -> new RegionInfoMenu(player, region, backButton));
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleBack(Player player, Region region, Runnable backButton) {
		return (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}
			if (!event.isLeftClick()) return;
			backButton.run();
		};
	}
}