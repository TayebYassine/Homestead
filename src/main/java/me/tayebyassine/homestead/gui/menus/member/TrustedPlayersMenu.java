package me.tayebyassine.homestead.gui.menus.member;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.PlayerLeftRegionEvent;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.gui.menus.region.PlayerInfoMenu;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionMember;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginated list of region members.
 *
 * <p>Left-click opens per-member player flags, right-click opens control flags, shift+right-click
 * opens player info, and shift+left-click untrusts the member.</p>
 */
public final class TrustedPlayersMenu {
    private static final String MENU_KEY = "region_trusted_players";

    private List<RegionMember> members;

    public TrustedPlayersMenu(Player player, Region region) {
        this.members = MemberManager.getMembersOfRegion(region);

        PaginationMenu.builder(MENU_KEY, 9 * 4)
                .nextPageItem(MenuButtons.getNextPageButton())
                .prevPageItem(MenuButtons.getPreviousPageButton())
                .items(getItems(player, region))
                .fillEmptySlots()
                .goBack((_player, event) -> new PlayersManagementMenu(player, region))
                .onClick((_player, context) -> handleMemberClick(player, region, context))
                .actionButton(1, MenuButtons.getButton(MENU_KEY, 1, new Placeholder()
                        .add("{max-members}", Limits.getRegionLimit(region, Limits.LimitType.MEMBERS_PER_REGION))), null)
                .build()
                .open(player);
    }

    private void handleMemberClick(Player player, Region region, PaginationMenu.ClickContext context) {
        if (context.index() >= members.size()) return;

        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        RegionMember member = members.get(context.index());

        if (member.getPlayer() == null || !MemberManager.isMemberOfRegion(region.getUniqueId(), member.getPlayerId())) {
            player.closeInventory();
            return;
        }

        if (context.event().isShiftClick() && context.event().isRightClick()) {
            new PlayerInfoMenu(player, member.getPlayer(), () -> new TrustedPlayersMenu(player, region));

        } else if (context.event().isRightClick()) {
            new MemberControlFlagsMenu(player, region, member);

        } else if (context.event().isShiftClick() && context.event().isLeftClick()) {
            handleUntrust(player, region, member, context);

        } else if (context.event().isLeftClick()) {
            new MemberPlayerFlagsMenu(player, region, member);
        }
    }

    private void handleUntrust(Player player, Region region, RegionMember member, PaginationMenu.ClickContext context) {
        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        if (!MemberManager.isMemberOfRegion(region.getUniqueId(), member.getPlayerId())) {
            player.closeInventory();
            return;
        }

        if (!player.hasPermission("homestead.actions.regions.players.untrust")) {
            Messages.send(player, "common.no_permission");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.UNTRUST_PLAYERS, true)) {
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        MemberManager.removeMemberFromRegion(member.getPlayer(), region);

        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

        LogManager.addLog(region, player, LogManager.PredefinedLog.UNTRUST_PLAYER, member.getPlayerName());

        Homestead.callEvent(new PlayerLeftRegionEvent(region, player));

        members = MemberManager.getMembersOfRegion(region);
        context.instance().setItems(getItems(player, region));
    }

    private List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();
        boolean taxesEnabled = Homestead.VAULT.isEconomyReady()
                && Resources.<RegionsFile>get(ResourceType.Regions).isTaxesEnabled();

        for (RegionMember member : members) {
            Placeholder placeholder = new Placeholder()
                    .add("{region}", region.getName())
                    .add("{playername}", member.getPlayerName())
                    .add("{member-joinedat}", Formatter.getDate(member.getJoinedAt()))
                    .add("{taxes-dueon}", taxesEnabled && region.getTaxes() > 0
                            ? Formatter.getRemainingTime(member.getTaxesAt())
                            : Formatter.getNever())
                    .add("{tax-amount}", Formatter.getBalance(region.getTaxes()));

            items.add(MenuButtons.getButton(MENU_KEY, 0, placeholder, member.getPlayer()));
        }

        return items;
    }
}
