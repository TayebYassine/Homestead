package me.tayebyassine.homestead.gui.menus.region;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionLog;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Paginated region activity log viewer.
 *
 * <p>Displays all log entries with mark-as-read (left-click) and delete (right-click) actions.
 * Includes bulk "mark all read" and "delete all" action buttons.</p>
 */
public final class RegionLogsMenu {
    private static final String MENU_KEY = "region_logs";

    private List<RegionLog> logs;

    public RegionLogsMenu(Player player, Region region) {
        this.logs = LogManager.getLogs(region);

        PaginationMenu.builder(MENU_KEY, 9 * 5)
                .nextPageItem(MenuButtons.getNextPageButton())
                .prevPageItem(MenuButtons.getPreviousPageButton())
                .items(getItems(player, region))
                .fillEmptySlots()
                .goBack((_player, event) -> new SelectedRegionMainMenu(player, region))
                .onClick((_player, context) -> handleLogClick(player, region, context))
                .actionButton(0, MenuButtons.getButton(MENU_KEY, 2), handleMarkAllRead(player, region))
                .actionButton(2, MenuButtons.getButton(MENU_KEY, 3), handleDeleteAll(player, region))
                .build()
                .open(player);
    }

    private static BiConsumer<Player, InventoryClickEvent> handleMarkAllRead(Player player, Region region) {
        return (_player, event) -> {
            if (!event.isLeftClick()) return;

            if (RegionManager.findRegion(region.getUniqueId()) == null) {
                player.closeInventory();
                return;
            }

            if (LogManager.getLogs(region).isEmpty()) {
                Messages.send(player, "commands.logs.1");
                return;
            }

            LogManager.markAllAsRead(region);


            Homestead.getInstance().runSyncTask(() -> new RegionLogsMenu(player, region));
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleDeleteAll(Player player, Region region) {
        return (_player, event) -> {
            if (!event.isLeftClick()) return;

            if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            if (LogManager.getLogs(region).isEmpty()) {
                Messages.send(player, "commands.logs.1");
                return;
            }

            if (RegionManager.findRegion(region.getUniqueId()) == null) {
                player.closeInventory();
                return;
            }

            LogManager.deleteLogsOfRegion(region);

            PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

            Homestead.getInstance().runSyncTask(() -> new RegionLogsMenu(player, region));
        };
    }

    private void handleLogClick(Player player, Region region, PaginationMenu.ClickContext context) {
        if (context.index() >= logs.size()) return;

        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_LOGS, true)) {
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        RegionLog log = logs.get(context.index());

        if (context.event().isLeftClick()) {
            log.setRead(true);
            refreshLogs(player, region, context);
        } else if (context.event().isRightClick()) {
            handleDeleteLog(player, region, log, context);
        }
    }

    private void handleDeleteLog(Player player, Region region, RegionLog log, PaginationMenu.ClickContext context) {
        if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
            Messages.send(player, "common.no_permission");
            return;
        }

        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        LogManager.deleteLog(log);
        refreshLogs(player, region, context);
    }

    private void refreshLogs(Player player, Region region, PaginationMenu.ClickContext context) {
        logs = LogManager.getLogs(region);
        context.instance().setItems(getItems(player, region));
    }

    private List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < logs.size(); i++) {
            RegionLog log = logs.get(i);

            Placeholder placeholder = new Placeholder()
                    .add("{region}", region.getName())
                    .add("{index}", i + 1)
                    .add("{log-sentat}", Formatter.getDate(log.getSentAt()))
                    .add("{log-author}", log.getAuthor())
                    .add("{log-message}", wrapMessage(log.getMessage()));

            items.add(MenuButtons.getButton(MENU_KEY, log.isRead() ? 1 : 0, placeholder));
        }

        return items;
    }

    private String wrapMessage(String message) {
        return MenuButtons.wrapMessage(message);
    }
}
