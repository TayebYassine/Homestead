package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;

import java.util.ArrayList;
import java.util.List;

public class SubAreasMenu {
	private final List<SubArea> subAreas;

	public SubAreasMenu(Player player, Region region) {
		subAreas = SubAreasManager.getSubAreasOfRegion(region.getUniqueId());

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(14), 9 * 4,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				getItems(player, region),
				(_player, event) -> new RegionMenu(player, region),
				(_player, context) -> {
					if (context.getIndex() >= subAreas.size()) return;

					SubArea subArea = subAreas.get(context.getIndex());

					if (context.getEvent().isLeftClick()) {
						new SubAreaMenu(player, region, subArea);
					}
				});

		gui.addActionButton(1, MenuUtils.getButton(72, new Placeholder()
				.add("{max-subareas}", Limits.getRegionLimit(region, Limits.LimitType.SUBAREAS_PER_REGION))
		), (_a, _b) -> {
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (SubArea subArea : subAreas) {
			items.add(MenuUtils.getButton(42, new Placeholder()
					.add("{region}", region.getName())
					.add("{subarea}", subArea.getName())
					.add("{subarea-volume}", subArea.getVolume())
					.add("{subarea-createdat}", Formatter.getDate(subArea.getCreatedAt()))));
		}

		return items;
	}
}