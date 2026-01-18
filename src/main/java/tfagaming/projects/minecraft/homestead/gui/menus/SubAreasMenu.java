package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubAreasMenu {
	List<SubArea> subAreas;

	public SubAreasMenu(Player player, Region region) {
		subAreas = SubAreasManager.getSubAreasOfRegion(region.getUniqueId());

		PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(14), 9 * 4,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(), getItems(player, region), (_player, event) -> {
			new RegionMenu(player, region);
		}, (_player, context) -> {
			if (context.getIndex() >= subAreas.size()) {
				return;
			}

			SubArea subArea = subAreas.get(context.getIndex());

			if (context.getEvent().isLeftClick()) {
				new SubAreaSettingsMenu(player, region, subArea);
			}
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	public List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < subAreas.size(); i++) {
			SubArea subArea = subAreas.get(i);

			HashMap<String, String> replacements = new HashMap<>();

			replacements.put("{region}", region.getName());
			replacements.put("{subarea}", subArea.getName());
			replacements.put("{subarea-volume}", String.valueOf(subArea.getVolume()));
			replacements.put("{subarea-createdat}", Formatters.formatDate(subArea.getCreatedAt()));

			items.add(MenuUtils.getButton(42, replacements));
		}

		return items;
	}
}
