package me.tayebyassine.homestead.gui.menus.region;

import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.managers.RateManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.RegionManager.RegionSorting;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.BiConsumer;

/**
 * Read-only region information display.
 *
 * <p>Shows region name, creation date, members, bank balance, rating, owner, and global rank.
 * Includes a button to open the rating menu.</p>
 */
public final class RegionInfoMenu {
    private static final String MENU_KEY = "region_info";

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

        Menu.builder(MENU_KEY, new Placeholder().add("{region}", region.getName()), 9 * 3)
                .item(11, MenuButtons.getButton(MENU_KEY, 0, placeholder))
                .item(13, MenuButtons.getButton(MENU_KEY, 1, placeholder))
                .button(15, MenuButtons.getButton(MENU_KEY, 2, placeholder), handleRating(player, region, backButton))
                .button(18, MenuButtons.getBackButton(), handleBack(player, region, backButton))
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
