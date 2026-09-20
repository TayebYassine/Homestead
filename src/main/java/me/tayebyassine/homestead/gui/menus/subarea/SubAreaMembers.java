package me.tayebyassine.homestead.gui.menus.subarea;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.player.PlayerJoinSubAreaEvent;
import me.tayebyassine.homestead.api.events.player.PlayerLeftSubAreaEvent;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.gui.menus.region.PlayerInfoMenu;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionMember;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.sessions.PlayerInputSession;
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
 * Paginated member list for a sub-area.
 *
 * <p>Left-click opens per-member flags, shift+right-click opens player info, shift+left-click
 * removes a member. Includes an "add member" action button.</p>
 */
public final class SubAreaMembers {
    private static final String MENU_KEY = "sub_area_members";
    private List<RegionMember> members;

    public SubAreaMembers(Player player, Region region, SubArea subArea) {
        this.members = MemberManager.getMembersOfSubArea(subArea);

        PaginationMenu.builder(MENU_KEY, 9 * 4)
                .nextPageItem(MenuButtons.getNextPageButton())
                .prevPageItem(MenuButtons.getPreviousPageButton())
                .items(getItems(player, region, subArea))
                .fillEmptySlots()
                .goBack((_player, event) -> new SubAreaMenu(player, region, subArea))
                .onClick((_player, context) -> handleMemberClick(player, region, subArea, context))
                .actionButton(1, MenuButtons.getButton(MENU_KEY, 0), handleAddMember(player, region, subArea))
                .build()
                .open(player);
    }

    private static BiConsumer<Player, InventoryClickEvent> handleAddMember(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (!event.isLeftClick()) return;

            if (RegionManager.findRegion(region.getUniqueId()) == null) {
                player.closeInventory();
                return;
            }

            if (!player.hasPermission("homestead.actions.regions.subareas.players.add")) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            player.closeInventory();

            PlayerInputSession.builder(Homestead.getInstance(), player)
                    .prompt(6)
                    .validator(msg -> validateAddMember(player, region, subArea, msg))
                    .callback((p, input) -> {
                        OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

                        if (targetPlayer == null) return;

                        MemberManager.addMemberToSubArea(targetPlayer, subArea);

                        LogManager.addLog(region, player, LogManager.PredefinedLog.ADD_PLAYER_SUBAREA, targetPlayer.getName(), subArea.getName());

                        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

                        Homestead.callEvent(new PlayerJoinSubAreaEvent(subArea, targetPlayer));

                        Homestead.getInstance().runSyncTask(() -> new SubAreaMembers(player, region, subArea));
                    })
                    .onCancel(p -> Homestead.getInstance().runSyncTask(() -> new SubAreaMembers(player, region, subArea)))
                    .build();
        };
    }

    private static boolean validateAddMember(Player player, Region region, SubArea subArea, String message) {
        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

        if (target == null) {
            Messages.send(player, "commands.trust.2", message);
            return false;
        }
        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_SUBAREAS, true)) {
            return false;
        }
        if (region.isOwner(target) || PlayerUtility.equals(player, target)) {
            Messages.send(player, "commands.trust.6");
            return false;
        }
        if (!MemberManager.isMemberOfRegion(region, target)) {
            Messages.send(player, "commands.trust.13");
            return false;
        }
        if (MemberManager.isMemberOfSubArea(subArea, target)) {
            Messages.send(player, "commands.trust.14");
            return false;
        }
        return true;
    }

    private void handleMemberClick(Player player, Region region, SubArea subArea, PaginationMenu.ClickContext context) {
        if (context.index() >= members.size()) return;

        if (RegionManager.findRegion(region.getUniqueId()) == null || SubAreaManager.findSubArea(subArea.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        RegionMember member = members.get(context.index());

        if (context.event().isShiftClick() && context.event().isRightClick()) {
            new PlayerInfoMenu(player, member.getPlayer(), () -> new SubAreaMembers(player, region, subArea));

        } else if (context.event().isShiftClick() && context.event().isLeftClick()) {
            handleRemoveMember(player, region, subArea, member, context);

        } else if (context.event().isLeftClick()) {
            new SubAreaMemberFlags(player, region, subArea, member);
        }
    }

    private void handleRemoveMember(Player player, Region region, SubArea subArea, RegionMember member, PaginationMenu.ClickContext context) {
        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        if (!MemberManager.isMemberOfRegion(region.getUniqueId(), member.getPlayerId())
                || !MemberManager.isMemberOfSubArea(subArea, player)) {
            return;
        }

        if (!player.hasPermission("homestead.actions.regions.subareas.players.remove")) {
            Messages.send(player, "common.no_permission");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_SUBAREAS, true)) {
            return;
        }

        MemberManager.removeMemberFromSubArea(member.getPlayer(), subArea);

        LogManager.addLog(region, player, LogManager.PredefinedLog.REMOVE_PLAYER_SUBAREA, member.getPlayerName(), subArea.getName());

        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

        Homestead.callEvent(new PlayerLeftSubAreaEvent(subArea, player));

        members = MemberManager.getMembersOfSubArea(subArea);
        context.instance().setItems(getItems(player, region, subArea));
    }

    private List<ItemStack> getItems(Player player, Region region, SubArea subArea) {
        List<ItemStack> items = new ArrayList<>();

        for (RegionMember member : members) {
            items.add(MenuButtons.getButton(MENU_KEY, 1, new Placeholder()
                            .add("{region}", region.getName())
                            .add("{subarea}", subArea.getName())
                            .add("{playername}", member.getPlayerName()),
                    member.getPlayer()));
        }

        return items;
    }
}
