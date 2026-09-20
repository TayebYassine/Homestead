package me.tayebyassine.homestead.gui.menus.member;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.player.BanPlayerEvent;
import me.tayebyassine.homestead.api.events.player.BulkUnbanPlayersEvent;
import me.tayebyassine.homestead.api.events.player.UnbanPlayerEvent;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.managers.*;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionBan;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.sessions.PlayerInputSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Paginated banned players list for a region.
 *
 * <p>Left-click unbans a player. Includes "ban player" (text input) and "unban all" action
 * buttons.</p>
 */
public final class BannedPlayersMenu {
    private static final String MENU_KEY = "region_banned_players";
    private List<RegionBan> bannedPlayers;

    public BannedPlayersMenu(Player player, Region region) {
        this.bannedPlayers = BanManager.getBansOfRegion(region);

        PaginationMenu gui = PaginationMenu.builder(MENU_KEY, 9 * 4)
                .nextPageItem(MenuButtons.getNextPageButton())
                .prevPageItem(MenuButtons.getPreviousPageButton())
                .items(getItems(player, region))
                .fillEmptySlots()
                .goBack((_player, event) -> new PlayersManagementMenu(player, region))
                .onClick((_player, context) -> handleUnban(player, region, context))
                .build();

        gui.addActionButton(0, MenuButtons.getButton(MENU_KEY, 0), handleBanPlayer(player, region))
                .addActionButton(2, MenuButtons.getButton(MENU_KEY, 2), handleUnbanAll(player, region));

        gui.open(player);
    }

    private static BiConsumer<Player, InventoryClickEvent> handleBanPlayer(Player player, Region region) {
        return (_player, event) -> {
            if (!event.isLeftClick()) return;

            if (RegionManager.findRegion(region.getUniqueId()) == null) {
                player.closeInventory();
                return;
            }

            if (!player.hasPermission("homestead.actions.regions.players.ban")) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            player.closeInventory();

            PlayerInputSession.builder(Homestead.getInstance(), player)
                    .prompt(5)
                    .validator(msg -> validateBan(player, region, msg))
                    .callback((p, input) -> {
                        OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

                        if (targetPlayer == null) return;

                        BanPlayerEvent banEvent = new BanPlayerEvent(region, targetPlayer, null);
                        Homestead.callEvent(banEvent);
                        if (banEvent.isCancelled()) return;

                        BanManager.banPlayer(region, targetPlayer, null);
                        if (MemberManager.isMemberOfRegion(region, targetPlayer))
                            MemberManager.removeMemberFromRegion(targetPlayer, region);
                        if (InviteManager.isInvited(region, targetPlayer))
                            InviteManager.deleteInvitesOfPlayer(region, targetPlayer);

                        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

                        LogManager.addLog(region, player, LogManager.PredefinedLog.BAN_PLAYER, targetPlayer.getName());

                        Homestead.getInstance().runSyncTask(() -> new BannedPlayersMenu(player, region));
                    })
                    .onCancel(p -> Homestead.getInstance().runSyncTask(() -> new BannedPlayersMenu(player, region)))
                    .build();
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleUnbanAll(Player player, Region region) {
        return (_player, event) -> {
            if (!event.isLeftClick()) return;

            if (RegionManager.findRegion(region.getUniqueId()) == null) {
                player.closeInventory();
                return;
            }

            if (!player.hasPermission("homestead.actions.regions.players.unban")) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.UNBAN_PLAYERS, true)) {
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            int bannedCount = BanManager.getBansOfRegion(region).size();
            if (bannedCount == 0) {
                Messages.send(player, "commands.unban.5");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            BanManager.unbanAllPlayers(region);

            LogManager.addLog(region, player, LogManager.PredefinedLog.PURGE_BANS);

            PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

            Homestead.callEvent(new BulkUnbanPlayersEvent(region));

            Homestead.getInstance().runSyncTask(() -> new BannedPlayersMenu(player, region));
        };
    }

    private static boolean validateBan(Player player, Region region, String message) {
        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

        if (target == null) {
            Messages.send(player, "commands.ban.3", message);
            return false;
        }
        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.BAN_PLAYERS, true)) {
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return false;
        }
        if (BanManager.isBanned(region, target)) {
            Messages.send(player, "commands.ban.5", target.getName());
            return false;
        }
        if (region.isOwner(target) || PlayerUtility.equals(player, target)) {
            Messages.send(player, "commands.ban.4");
            return false;
        }
        SeRent rent = region.getRent();
        if (rent != null && rent.isRenterer(target)) {
            Messages.send(player, "commands.ban.6", target.getName());
            return false;
        }
        return true;
    }

    private void handleUnban(Player player, Region region, PaginationMenu.ClickContext context) {
        if (context.index() >= bannedPlayers.size()) return;

        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        if (!player.hasPermission("homestead.actions.regions.players.unban")) {
            Messages.send(player, "common.no_permission");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        RegionBan bannedPlayer = bannedPlayers.get(context.index());
        if (!context.event().isLeftClick()) return;
        if (!BanManager.isBanned(region, bannedPlayer.getPlayer())) return;

        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.UNBAN_PLAYERS, true)) {
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        UnbanPlayerEvent unbanEvent = new UnbanPlayerEvent(region, bannedPlayer.getPlayer());
        Homestead.callEvent(unbanEvent);
        if (unbanEvent.isCancelled()) return;

        BanManager.unbanPlayer(region, bannedPlayer.getPlayer());
        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

        LogManager.addLog(region, player, LogManager.PredefinedLog.UNBAN_PLAYER, bannedPlayer.getPlayerName());

        bannedPlayers = BanManager.getBansOfRegion(region);
        context.instance().setItems(getItems(player, region));
    }

    private List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();

        for (RegionBan bannedPlayer : bannedPlayers) {
            Placeholder placeholder = new Placeholder()
                    .add("{region}", region.getName())
                    .add("{playername}", bannedPlayer.getPlayerName())
                    .add("{player-bannedat}", Formatter.getDate(bannedPlayer.getBannedAt()))
                    .add("{player-banreason}", wrapMessage(bannedPlayer.getReason()));

            items.add(MenuButtons.getButton(MENU_KEY, 1, placeholder, bannedPlayer.getPlayer()));
        }

        return items;
    }

    private String wrapMessage(String message) {
        return MenuButtons.wrapMessage(message);
    }
}
