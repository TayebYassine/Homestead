package me.tayebyassine.homestead.gui.menus.region;

import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.BiConsumer;

/**
 * Region rewards/bank information menu.
 *
 * <p>Displays reward calculation details based on members, chunks, and sub-areas.</p>
 */
public final class Rewards {
    private static final String MENU_KEY = "rewards";

    public Rewards(Player player, Region region, Runnable backButton) {
        Menu.builder(MENU_KEY, new Placeholder().add("{region}", region.getName()), 9 * 3)
                .item(12, MenuButtons.getButton(MENU_KEY, 0, new Placeholder()
                        .add("{region}", region.getName())
                        .add("{members}", MemberManager.getMembersOfRegion(region).size())
                        .add("{chunks}", me.tayebyassine.homestead.util.minecraft.rewards.Rewards.getChunksByEachMember(region))
                        .add("{subareas}", me.tayebyassine.homestead.util.minecraft.rewards.Rewards.getSubAreasByEachMember(region))))
                .item(14, MenuButtons.getButton(MENU_KEY, 1, new Placeholder()
                        .add("{region}", region.getName())
                        .add("{members}", MemberManager.getMembersOfRegion(region).size())
                        .add("{player-playtime}", Formatter.getPlayerPlaytime(player))
                        .add("{chunks}", me.tayebyassine.homestead.util.minecraft.rewards.Rewards.getChunksByPlayTime(player))
                        .add("{subareas}", me.tayebyassine.homestead.util.minecraft.rewards.Rewards.getSubAreasByPlayTime(player))))
                .button(18, MenuButtons.getBackButton(), handleBack(player, region, backButton))
                .fillEmptySlots()
                .build()
                .open(player);
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