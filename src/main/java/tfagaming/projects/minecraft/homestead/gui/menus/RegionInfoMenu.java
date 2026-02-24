package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager.RegionSorting;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;

public class RegionInfoMenu {
	public RegionInfoMenu(Player player, Region region, Runnable backButton) {
		Menu gui = new Menu(MenuUtils.getTitle(8).replace("{region}", region.getName()), 9 * 3);

		Placeholder placeholder = new Placeholder()
				.add("{region}", region.getName())
				.add("{region-createdat}", Formatter.getDate(region.getCreatedAt()))
				.add("{region-members}", Formatter.getMembersOfRegion(region))
				.add("{region-bank}", Formatter.getBalance(region.getBank()))
				.add("{region-rating}", Formatter.getRating(RegionsManager.getAverageRating(region)))
				.add("{region-owner}", region.getOwner().getName())
				.add("{region-global-rank}", RegionsManager.getGlobalRank(region.getUniqueId()))
				.add("{region-rank-bank}", RegionsManager.getRank(RegionSorting.BANK, region.getUniqueId()))
				.add("{region-rank-chunks}", RegionsManager.getRank(RegionSorting.CHUNKS_COUNT, region.getUniqueId()))
				.add("{region-rank-members}", RegionsManager.getRank(RegionSorting.MEMBERS_COUNT, region.getUniqueId()))
				.add("{region-rank-rating}", RegionsManager.getRank(RegionSorting.RATING, region.getUniqueId()));

		gui.addItem(11, MenuUtils.getButton(25, placeholder), null);
		gui.addItem(13, MenuUtils.getButton(26, placeholder), null);

		gui.addItem(15, MenuUtils.getButton(61, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new RegionRating(player, region, () -> new RegionInfoMenu(player, region, backButton));
		});

		gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
			if (!event.isLeftClick()) return;
			backButton.run();
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}
}