package me.tayebyassine.homestead.gui.menus.subarea;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.gui.menus.rent.RentConfigMenu;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.PlayerInputSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.java.StringUtils;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.BiConsumer;

/**
 * Sub-area management hub menu.
 *
 * <p>Provides rename, flags, members, end rent, and delete actions for a specific sub-area.</p>
 */
public final class SubAreaMenu {
    private static final String MENU_KEY = "sub_area";

    public SubAreaMenu(Player player, Region region, SubArea subArea) {
        boolean isEconomyEnabled = Homestead.VAULT.isEconomyReady();
        boolean isRentEnabled = isEconomyEnabled && Resources.<RegionsFile>get(ResourceType.Regions).isRentingEnabled();

        SeRent rent = subArea.getRent();

        Placeholder placeholder = new Placeholder()
                .add("{subarea}", subArea.getName())
                .add("{subarea-players}", MemberManager.getMembersOfRegion(region).size())
                .add("{rent-enabled}", Formatter.getToggle(isRentEnabled));

        Menu.builder(MENU_KEY, new Placeholder().add("{subarea}", subArea.getName()), 9 * 3)
                .button(11, MenuButtons.getButton(MENU_KEY, 0, placeholder), handleRename(player, region, subArea))
                .button(12, MenuButtons.getButton(MENU_KEY, 1, placeholder), handleFlags(player, region, subArea))
                .button(13, MenuButtons.getButton(MENU_KEY, 2, placeholder), handleMembers(player, region, subArea))
                .button(14, MenuButtons.getButton(MENU_KEY, 3, placeholder), handleOpenRentMenu(player, region, subArea))
                .button(15, MenuButtons.getButton(MENU_KEY, 4, placeholder), handleDelete(player, region, subArea))
                .button(18, MenuButtons.getBackButton(), handleBack(player, region, subArea))
                .fillEmptySlots()
                .build()
                .open(player);
    }

    private static BiConsumer<Player, InventoryClickEvent> handleRename(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

            if (RegionManager.findRegion(region.getUniqueId()) == null) {
                player.closeInventory();
                return;
            }

            if (!player.hasPermission("homestead.actions.regions.subareas.update.name")) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            player.closeInventory();

            PlayerInputSession.builder(Homestead.getInstance(), player)
                    .prompt(7)
                    .validator(msg -> validateRename(player, region, subArea, msg))
                    .callback((p, input) -> {
                        subArea.setName(input);
                        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
                        Homestead.getInstance().runSyncTask(() -> new SubAreaMenu(player, region, subArea));
                    })
                    .onCancel(p -> Homestead.getInstance().runSyncTask(() -> new SubAreaMenu(player, region, subArea)))
                    .build();
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleFlags(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (checkValid(player, region, subArea) && event.isLeftClick()) {
                new SubAreaFlagsMenu(player, region, subArea);
            }
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleMembers(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (checkValid(player, region, subArea) && event.isLeftClick()) {
                new SubAreaMembers(player, region, subArea);
            }
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleOpenRentMenu(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

            if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            new RentConfigMenu(player, region, subArea);
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleDelete(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

            if (!player.hasPermission("homestead.actions.regions.subareas.delete")) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }
            if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_SUBAREAS, true)) {
                return;
            }

            SubAreaManager.deleteSubArea(subArea.getUniqueId());

            LogManager.addLog(region, player, LogManager.PredefinedLog.DELETE_SUBAREA);

            PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

            new SubAreasMenu(player, region);
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleBack(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (checkValid(player, region, subArea) && event.isLeftClick()) {
                new SubAreasMenu(player, region);
            }
        };
    }

    private static boolean validateRename(Player player, Region region, SubArea subArea, String message) {
        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_SUBAREAS, true)) {
            return false;
        }
        if (!StringUtils.isValidSubAreaName(message)) {
            Messages.send(player, "commands.subareas.5");
            return false;
        }
        if (subArea.getName().equalsIgnoreCase(message)) {
            Messages.send(player, "commands.subareas.12");
            return false;
        }
        if (SubAreaManager.isNameUsed(region.getUniqueId(), message)) {
            Messages.send(player, "commands.subareas.6");
            return false;
        }
        if (ColorTranslator.containsMiniMessageTag(message)) {
            Messages.send(player, "commands.subareas.13");
            return false;
        }
        return true;
    }

    private static boolean checkValid(Player player, Region region, SubArea subArea) {
        if (RegionManager.findRegion(region.getUniqueId()) == null || SubAreaManager.findSubArea(subArea.getUniqueId()) == null) {
            player.closeInventory();
            return false;
        }
        return true;
    }
}
