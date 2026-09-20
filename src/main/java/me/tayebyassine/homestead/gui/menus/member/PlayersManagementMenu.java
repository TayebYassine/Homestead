package me.tayebyassine.homestead.gui.menus.member;

import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.gui.menus.region.SelectedRegionMainMenu;
import me.tayebyassine.homestead.managers.BanManager;
import me.tayebyassine.homestead.managers.InviteManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.BiConsumer;

/**
 * Player management hub for a region.
 *
 * <p>Provides navigation to the members list, banned players list, and invited players list.</p>
 */
public final class PlayersManagementMenu {
    private static final String MENU_KEY = "region_players_management";

    public PlayersManagementMenu(Player player, Region region) {
        Placeholder placeholder = new Placeholder()
                .add("{region-members}", MemberManager.getMembersOfRegion(region).size())
                .add("{region-members-max}", Limits.getRegionLimit(region, Limits.LimitType.MEMBERS_PER_REGION))
                .add("{region-banned-players}", BanManager.getBansOfRegion(region).size())
                .add("{region-invited-players}", InviteManager.getInvitesOfRegion(region).size());

        Menu.builder(MENU_KEY, 9 * 3)
                .button(11, MenuButtons.getButton(MENU_KEY, 0, placeholder), handleMembers(player, region))
                .button(13, MenuButtons.getButton(MENU_KEY, 1, placeholder), handleBanned(player, region))
                .button(15, MenuButtons.getButton(MENU_KEY, 2, placeholder), handleInvited(player, region))
                .button(18, MenuButtons.getBackButton(), handleBack(player, region))
                .fillEmptySlots()
                .build()
                .open(player);
    }

    private static BiConsumer<Player, InventoryClickEvent> handleMembers(Player player, Region region) {
        return (_player, event) -> {
            if (checkRegion(player, region) && event.isLeftClick()) {
                new TrustedPlayersMenu(player, region);
            }
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleBanned(Player player, Region region) {
        return (_player, event) -> {
            if (checkRegion(player, region) && event.isLeftClick()) {
                new BannedPlayersMenu(player, region);
            }
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleInvited(Player player, Region region) {
        return (_player, event) -> {
            if (checkRegion(player, region) && event.isLeftClick()) {
                new InvitedPlayersMenu(player, region);
            }
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleBack(Player player, Region region) {
        return (_player, event) -> {
            if (checkRegion(player, region) && event.isLeftClick()) {
                new SelectedRegionMainMenu(player, region);
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
