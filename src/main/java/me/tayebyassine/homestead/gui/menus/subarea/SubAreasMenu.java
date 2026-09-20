package me.tayebyassine.homestead.gui.menus.subarea;

import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.gui.menus.region.SelectedRegionMainMenu;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginated list of sub-areas within a region.
 *
 * <p>Left-click opens the sub-area management menu for the selected sub-area.</p>
 */
public final class SubAreasMenu {
    private static final String MENU_KEY = "sub_areas";
    private final List<SubArea> subAreas;

    public SubAreasMenu(Player player, Region region) {
        this.subAreas = SubAreaManager.getSubAreasOfRegion(region.getUniqueId());

        PaginationMenu.builder(MENU_KEY, 9 * 4)
                .nextPageItem(MenuButtons.getNextPageButton())
                .prevPageItem(MenuButtons.getPreviousPageButton())
                .items(getItems(player, region))
                .fillEmptySlots()
                .goBack((_player, event) -> new SelectedRegionMainMenu(player, region))
                .onClick((_player, context) -> handleSubAreaClick(player, region, context))
                .actionButton(1, MenuButtons.getButton(MENU_KEY, 1, new Placeholder()
                        .add("{max-subareas}", Limits.getRegionLimit(region, Limits.LimitType.SUBAREAS_PER_REGION))), null)
                .build()
                .open(player);
    }

    private void handleSubAreaClick(Player player, Region region, PaginationMenu.ClickContext context) {
        if (context.index() >= subAreas.size()) return;

        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        SubArea subArea = subAreas.get(context.index());

        if (context.event().isLeftClick()) {
            new SubAreaMenu(player, region, subArea);
        }
    }

    private List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();

        for (SubArea subArea : subAreas) {
            items.add(MenuButtons.getButton(MENU_KEY, 0, new Placeholder()
                    .add("{region}", region.getName())
                    .add("{subarea}", subArea.getName())
                    .add("{subarea-volume}", subArea.getVolume())
                    .add("{subarea-createdat}", Formatter.getDate(subArea.getCreatedAt()))));
        }

        return items;
    }
}
