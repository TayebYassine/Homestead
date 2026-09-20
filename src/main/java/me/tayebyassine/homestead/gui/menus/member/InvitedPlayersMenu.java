package me.tayebyassine.homestead.gui.menus.member;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.player.BulkDeleteInvitesEvent;
import me.tayebyassine.homestead.api.events.player.InvitePlayerEvent;
import me.tayebyassine.homestead.api.events.player.PlayerJoinRegionEvent;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.managers.*;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionInvite;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.PlayerInputSession;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
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
 * Paginated invited players list for a region.
 *
 * <p>Left-click cancels an invite. Includes "invite player" (text input) and "clear all invites"
 * action buttons.</p>
 */
public final class InvitedPlayersMenu {
    private static final String MENU_KEY = "region_invited_players";
    private List<RegionInvite> invites;

    public InvitedPlayersMenu(Player player, Region region) {
        this.invites = InviteManager.getInvitesOfRegion(region);

        PaginationMenu.builder(MENU_KEY, 9 * 4)
                .nextPageItem(MenuButtons.getNextPageButton())
                .prevPageItem(MenuButtons.getPreviousPageButton())
                .items(getItems(player, region))
                .fillEmptySlots()
                .goBack((_player, event) -> new PlayersManagementMenu(player, region))
                .onClick((_player, context) -> handleInviteClick(player, region, context))
                .actionButton(0, MenuButtons.getButton(MENU_KEY, 0), handleInvitePlayer(player, region))
                .actionButton(2, MenuButtons.getButton(MENU_KEY, 2), handleClearInvites(player, region))
                .build()
                .open(player);
    }

    private static BiConsumer<Player, InventoryClickEvent> handleInvitePlayer(Player player, Region region) {
        return (_player, event) -> {
            if (RegionManager.findRegion(region.getUniqueId()) == null) {
                player.closeInventory();
                return;
            }
            if (!event.isLeftClick()) return;

            if (!player.hasPermission("homestead.actions.regions.players.trust")) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            player.closeInventory();

            PlayerInputSession.builder(Homestead.getInstance(), player)
                    .prompt(6)
                    .validator(msg -> validateInvite(player, region, msg))
                    .callback((p, input) -> {
                        OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);
                        if (targetPlayer == null) return;

                        if (Resources.<RegionsFile>get(ResourceType.Regions).isInstantTrustSystemEnabled()) {
                            MemberManager.addMemberToRegion(targetPlayer, region);

                            LogManager.addLog(region, targetPlayer, LogManager.PredefinedLog.JOIN_REGION);

                            Homestead.callEvent(new PlayerJoinRegionEvent(region, targetPlayer));
                        } else {
                            InviteManager.invitePlayer(region, targetPlayer);

                            LogManager.addLog(region, player, LogManager.PredefinedLog.INVITE_PLAYER, targetPlayer.getName());

                            if (targetPlayer.isOnline()) {
                                Messages.send(player, "commands.trust.11", region.getName());
                            }

                            Homestead.callEvent(new InvitePlayerEvent(region, targetPlayer));
                        }

                        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
                        Homestead.getInstance().runSyncTask(() -> new InvitedPlayersMenu(player, region));
                    })
                    .onCancel(p -> Homestead.getInstance().runSyncTask(() -> new InvitedPlayersMenu(player, region)))
                    .build();
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleClearInvites(Player player, Region region) {
        return (_player, event) -> {
            if (RegionManager.findRegion(region.getUniqueId()) == null) {
                player.closeInventory();
                return;
            }
            if (!event.isLeftClick()) return;

            if (InviteManager.getInvitesOfRegion(region).isEmpty()) {
                Messages.send(player, "commands.untrust.7");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            InviteManager.deleteInvitesOfRegion(region);

            PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

            LogManager.addLog(region, player, LogManager.PredefinedLog.PURGE_INVITES);

            Homestead.callEvent(new BulkDeleteInvitesEvent(region));

            Homestead.getInstance().runSyncTask(() -> new InvitedPlayersMenu(player, region));
        };
    }

    private static boolean validateInvite(Player player, Region region, String message) {
        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

        if (target == null) {
            Messages.send(player, "commands.trust.2", message);
            return false;
        }
        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.TRUST_PLAYERS, true)) {
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return false;
        }
        if (BanManager.isBanned(region, target)) {
            Messages.send(player, "commands.trust.3");
            return false;
        }
        if (MemberManager.isMemberOfRegion(region, target)) {
            Messages.send(player, "commands.trust.4");
            return false;
        }
        if (InviteManager.isInvited(region, target)) {
            Messages.send(player, "commands.trust.5");
            return false;
        }
        if (region.isOwner(target) || PlayerUtility.equals(player, target)) {
            Messages.send(player, "commands.trust.6");
            return false;
        }

        SeRent rent = region.getRent();
        if (rent != null && rent.isRenterer(target)) {
            Messages.send(player, "commands.trust.7");
            return false;
        }
        if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
            Messages.send(player, "commands.trust.8");
            return false;
        }
        return true;
    }

    private void handleInviteClick(Player player, Region region, PaginationMenu.ClickContext context) {
        if (context.index() >= invites.size()) return;

        RegionInvite invite = invites.get(context.index());
        if (!context.event().isLeftClick()) return;
        if (!InviteManager.isInvited(region, invite.getPlayer())) return;

        InviteManager.deleteInvite(invite.getUniqueId());
        invites = InviteManager.getInvitesOfRegion(region);
        context.instance().setItems(getItems(player, region));
    }

    private List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();

        for (RegionInvite invite : invites) {
            items.add(MenuButtons.getButton(MENU_KEY, 1, new Placeholder()
                    .add("{playername}", invite.getPlayerName())));
        }

        return items;
    }
}
