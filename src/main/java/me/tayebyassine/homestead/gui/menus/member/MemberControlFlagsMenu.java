package me.tayebyassine.homestead.gui.menus.member;

import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.flags.FlagCalculator;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionMember;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.FlagsFile;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-member control flag configuration menu.
 *
 * <p>Allows the region owner/operator to toggle control flags (e.g., {@link ControlFlag#MANAGE_SUBAREAS},
 * {@link ControlFlag#UNTRUST_PLAYERS}) for a specific member.</p>
 */
public final class MemberControlFlagsMenu {
    private static final String MENU_KEY = "region_member_control_flags";

    public MemberControlFlagsMenu(Player player, Region region, RegionMember member) {
        List<ItemStack> items = new ArrayList<>();

        for (String flagString : ControlFlag.getFlags()) {
            boolean value = FlagCalculator.isFlagSet(member.getControlFlags(), ControlFlag.parse(flagString));
            items.add(MenuButtons.getFlagButton(MENU_KEY, 0, flagString, value));
        }

        PaginationMenu.builder(MENU_KEY, new Placeholder().add("{playername}", member.getPlayerName()), 9 * 5)
                .nextPageItem(MenuButtons.getNextPageButton())
                .prevPageItem(MenuButtons.getPreviousPageButton())
                .items(items)
                .fillEmptySlots()
                .goBack((_player, event) -> new TrustedPlayersMenu(player, region))
                .onClick((_player, context) -> handleFlagClick(player, region, member, context))
                .build()
                .open(player);
    }

    private void handleFlagClick(Player player, Region region, RegionMember member, PaginationMenu.ClickContext context) {
        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        if (!MemberManager.isMemberOfRegion(region.getUniqueId(), member.getPlayerId())) {
            player.closeInventory();
            return;
        }

        if (Cooldown.hasCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE)) return;

        if (!player.hasPermission("homestead.actions.regions.update.flags.members")) {
            Messages.send(player, "common.no_permission");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
            Messages.send(player, "common.no_permission");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        String flagString = ControlFlag.getFlags().get(context.index());

        if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
            Messages.send(player, "commands.flags.9");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        if (!context.event().isLeftClick()) return;

        long flags = member.getControlFlags();
        long flag = ControlFlag.parse(flagString);
        boolean isSet = FlagCalculator.isFlagSet(flags, flag);

        Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

        member.setControlFlags(isSet
                ? FlagCalculator.removeFlag(flags, flag)
                : FlagCalculator.addFlag(flags, flag));

        PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

        context.instance().replaceSlot(context.index(), MenuButtons.getFlagButton(MENU_KEY, 0, flagString, !isSet));
    }
}
