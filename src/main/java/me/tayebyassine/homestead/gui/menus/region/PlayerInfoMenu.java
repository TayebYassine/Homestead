package me.tayebyassine.homestead.gui.menus.region;

import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.players.PlayerBank;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Read-only player information display.
 *
 * <p>Shows target player's region count, name, online status, balance, ping, join date, owned
 * regions, and trusted regions.</p>
 */
public final class PlayerInfoMenu {
    private static final String MENU_KEY = "player_info";

    public PlayerInfoMenu(Player player, OfflinePlayer target, Runnable backButton) {
        Placeholder placeholder = new Placeholder()
                .add("{regions-count}", RegionManager.getRegionsOwnedByPlayer(target).size()
                        + RegionManager.getRegionsHasPlayerAsMember(target).size())
                .add("{playername}", target.getName())
                .add("{player-status}", Formatter.getPlayerStatus(target))
                .add("{player-balance}", Formatter.getBalance(PlayerBank.get(target)))
                .add("{player-ping}", target.isOnline() ? ((Player) target).getPing() : 0)
                .add("{player-joinedat}", Formatter.getDate(target.getFirstPlayed()))
                .add("{player-owned-regions}", Formatter.getPlayerOwnedRegions(target))
                .add("{player-trusted-regions}", Formatter.getPlayerTrustedRegions(target));

        Menu.builder(MENU_KEY, 9 * 3)
                .item(11, MenuButtons.getButton(MENU_KEY, 21, placeholder, target))
                .item(13, MenuButtons.getButton(MENU_KEY, 22, placeholder))
                .item(15, MenuButtons.getButton(MENU_KEY, 23, placeholder))
                .button(18, MenuButtons.getBackButton(), (_player, event) -> {
                    if (!event.isLeftClick()) return;
                    backButton.run();
                })
                .build()
                .fillEmptySlots()
                .open(player);
    }
}
