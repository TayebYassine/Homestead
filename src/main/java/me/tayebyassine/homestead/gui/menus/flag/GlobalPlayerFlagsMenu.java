package me.tayebyassine.homestead.gui.menus.flag;

import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.flags.FlagCalculator;
import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.gui.menus.region.SelectedRegionMainMenu;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.FlagsFile;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Global player flag configuration menu for a region.
 *
 * <p>Allows toggling player flags that apply globally to all non-member players within the
 * region.</p>
 */
public final class GlobalPlayerFlagsMenu {
    private static final String MENU_KEY = "region_global_player_flags";

    public GlobalPlayerFlagsMenu(Player player, Region region) {
        List<ItemStack> flagItems = new ArrayList<>();

        for (String flagString : PlayerFlag.getFlags()) {
            boolean value = FlagCalculator.isFlagSet(region.getPlayerFlags(), PlayerFlag.parse(flagString));
            flagItems.add(MenuButtons.getFlagButton(MENU_KEY, 0, flagString, value));
        }

        PaginationMenu gui = PaginationMenu.builder(MENU_KEY, 9 * 5)
                .nextPageItem(MenuButtons.getNextPageButton())
                .prevPageItem(MenuButtons.getPreviousPageButton())
                .items(flagItems)
                .fillEmptySlots()
                .goBack((_player, event) -> new SelectedRegionMainMenu(player, region))
                .onClick((_player, context) -> handleFlagClick(player, region, context))
                .build();

        gui.open(player);
    }

    private void handleFlagClick(Player player, Region region, PaginationMenu.ClickContext context) {
        if (!context.event().isLeftClick()) return;

        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        if (Cooldown.hasCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE)) return;

        if (!player.hasPermission("homestead.actions.regions.update.flags.global")) {
            Messages.send(player, "common.no_permission");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.SET_GLOBAL_FLAGS, true)) {
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        String flagString = PlayerFlag.getFlags().get(context.index());

        if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
            Messages.send(player, "commands.flags.9");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        long flags = region.getPlayerFlags();
        long flag = PlayerFlag.parse(flagString);
        boolean isSet = FlagCalculator.isFlagSet(flags, flag);

        Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

        region.setPlayerFlags(isSet
                ? FlagCalculator.removeFlag(flags, flag)
                : FlagCalculator.addFlag(flags, flag));

        PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

        context.instance().replaceSlot(context.index(), MenuButtons.getFlagButton(MENU_KEY, 0, flagString, !isSet));
    }
}
