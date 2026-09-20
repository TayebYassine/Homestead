package me.tayebyassine.homestead.gui.menus.rent;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RentConfigUpdateEvent;
import me.tayebyassine.homestead.api.events.RentEndEvent;
import me.tayebyassine.homestead.api.events.RentNoticeEvent;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.gui.menus.region.SelectedRegionMainMenu;
import me.tayebyassine.homestead.gui.menus.subarea.SubAreaMenu;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeNoticeToVacate;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.LanguageFile;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.PlayerInputSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerBank;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Rent configuration menu for a region or sub-area.
 *
 * <p>Allows setting price, duration, permanent mode, security deposit, notice-to-vacate, cancel
 * rent, and end rent. Works for both region-level and sub-area-level rent.</p>
 */
public final class RentConfigMenu {
    private static final String MENU_KEY = "rent_config";

    public RentConfigMenu(Player player, Region region, SubArea subArea) {
        RegionsFile regionsConfig = Resources.get(ResourceType.Regions);
        boolean isRentEnabled = Homestead.VAULT.isEconomyReady() && regionsConfig.isRentingEnabled();

        SeRent rent = subArea != null ? subArea.getRent() : region.getRent();

        Placeholder placeholder = new Placeholder()
                .add("{region}", region.getName())
                .add("{subarea}", subArea != null ? subArea.getName() : Formatter.getNA())
                .add("{rent-enabled}", Formatter.getToggle(isRentEnabled))
                .add("{rent-renter}", rent.hasRenter() ? rent.getRenterName() : Formatter.getNone())
                .add("{rent-price}", rent.getPrice() > 0 ? Formatter.getBalance(rent.getPrice()) : Formatter.getNone())
                .add("{min-price}", Formatter.getBalance(regionsConfig.getMinRentPrice()))
                .add("{max-price}", Formatter.getBalance(regionsConfig.getMaxRentPrice()))
                .add("{rent-duration}", rent.getDuration() > 0 ? Formatter.getDuration(rent.getDuration()) : (rent.isPermanent() ? Formatter.getPermanent() : Formatter.getNone()))
                .add("{min-days}", regionsConfig.getMinRentDays())
                .add("{max-days}", regionsConfig.getMaxRentDays())
                .add("{rent-until}", rent.getUntilAt() > 0 ? Formatter.getRemainingTime(rent.getUntilAt()) : Formatter.getNever())
                .add("{rent-deposit}", rent.getSecurityDeposit() > 0 ? Formatter.getBalance(rent.getSecurityDeposit()) : Formatter.getNone())
                .add("{min-deposit}", Formatter.getBalance(regionsConfig.getMinSecurityDeposit()))
                .add("{max-deposit}", Formatter.getBalance(regionsConfig.getMaxSecurityDeposit()))
                .add("{rent-notice}", rent.hasNoticeToVacate()
                        ? Formatter.applyPlaceholders(
                        Resources.<LanguageFile>get(ResourceType.Language).getVariableRentVacateNotice(),
                        new Placeholder().add("{days}", Objects.requireNonNull(rent.getNoticeToVacate()).getDaysToVacate())
                )
                        : Formatter.getNone());

        Menu.builder(MENU_KEY, new Placeholder().add("{region-or-subarea}", subArea != null ? subArea.getName() : region.getName()), 9 * 3)
                .button(10, MenuButtons.getButton(MENU_KEY, 0, placeholder), handlePrice(player, region, subArea, placeholder))
                .button(11, MenuButtons.getButton(MENU_KEY, 1, placeholder), handleDuration(player, region, subArea, placeholder))
                .button(12, MenuButtons.getButton(MENU_KEY, 2, placeholder), handleSetPermanent(player, region, subArea))
                .button(13, MenuButtons.getButton(MENU_KEY, 3, placeholder), handleSecurityDeposit(player, region, subArea, placeholder))
                .button(14, MenuButtons.getButton(MENU_KEY, 4, placeholder), handleNoticeToVacate(player, region, subArea))
                .button(15, MenuButtons.getButton(MENU_KEY, 5, placeholder), handleCancelRent(player, region, subArea))
                .button(16, MenuButtons.getButton(MENU_KEY, 6, placeholder), handleEndRent(player, region, subArea))
                .button(18, MenuButtons.getBackButton(), handleBack(player, region, subArea))
                .fillEmptySlots()
                .build()
                .open(player);
    }

    private static BiConsumer<Player, InventoryClickEvent> handlePrice(Player player, Region region, SubArea subArea, Placeholder placeholder) {
        return (_player, event) -> {
            if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

            if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_SUBAREAS, true)) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            player.closeInventory();

            RegionsFile config = Resources.get(ResourceType.Regions);
            double min = config.getMinRentPrice();
            double max = config.getMaxRentPrice();

            PlayerInputSession.builder(Homestead.getInstance(), player)
                    .prompt(10, placeholder)
                    .validator(msg -> validatePrice(player, msg, min, max))
                    .callback((p, input) -> {
                        double price = Double.parseDouble(input);
                        SeRent rent = subArea != null ? subArea.getRent() : region.getRent();
                        double oldPrice = rent.getPrice();
                        rent.setPrice(price);
                        Homestead.callEvent(new RentConfigUpdateEvent(region, subArea, rent, "price", oldPrice, price));
                        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
                        Homestead.getInstance().runSyncTask(() -> new RentConfigMenu(player, region, subArea));
                    })
                    .onCancel(p -> Homestead.getInstance().runSyncTask(() -> new RentConfigMenu(player, region, subArea)))
                    .build();
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleDuration(Player player, Region region, SubArea subArea, Placeholder placeholder) {
        return (_player, event) -> {
            if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

            if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_SUBAREAS, true)) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            player.closeInventory();

            RegionsFile config = Resources.get(ResourceType.Regions);
            int minDays = config.getMinRentDays();
            int maxDays = config.getMaxRentDays();

            PlayerInputSession.builder(Homestead.getInstance(), player)
                    .prompt(11, placeholder)
                    .validator(msg -> validateDuration(player, msg, minDays, maxDays))
                    .callback((p, input) -> {
                        long days = Long.parseLong(input);
                        long duration = days == -1 ? -1L : days * 24L * 60 * 60 * 1000;
                        SeRent rent = subArea != null ? subArea.getRent() : region.getRent();

                        if (rent.hasRenter() && rent.hasNoticeToVacate()) {
                            Messages.send(player, "commands.rent.15");
                            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                            return;
                        }

                        long oldDuration = rent.getDuration();
                        rent.setDuration(duration);
                        if (days != -1 && rent.getStartedAt() > 0) {
                            rent.setUntilAt(rent.getStartedAt() + duration);
                        } else if (days == -1) {
                            rent.setUntilAt(-1L);
                        }

                        Homestead.callEvent(new RentConfigUpdateEvent(region, subArea, rent, "duration", oldDuration, duration));

                        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
                        Homestead.getInstance().runSyncTask(() -> new RentConfigMenu(player, region, subArea));
                    })
                    .onCancel(p -> Homestead.getInstance().runSyncTask(() -> new RentConfigMenu(player, region, subArea)))
                    .build();
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleSecurityDeposit(Player player, Region region, SubArea subArea, Placeholder placeholder) {
        return (_player, event) -> {
            if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

            if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_SUBAREAS, true)) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            player.closeInventory();

            RegionsFile config = Resources.get(ResourceType.Regions);
            double min = config.getMinSecurityDeposit();
            double max = config.getMaxSecurityDeposit();

            PlayerInputSession.builder(Homestead.getInstance(), player)
                    .prompt(12, placeholder)
                    .validator(msg -> validatePrice(player, msg, min, max))
                    .callback((p, input) -> {
                        double deposit = Double.parseDouble(input);
                        SeRent rent = subArea != null ? subArea.getRent() : region.getRent();
                        double oldDeposit = rent.getSecurityDeposit();
                        rent.setSecurityDeposit(deposit);
                        Homestead.callEvent(new RentConfigUpdateEvent(region, subArea, rent, "securityDeposit", oldDeposit, deposit));
                        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
                        Homestead.getInstance().runSyncTask(() -> new RentConfigMenu(player, region, subArea));
                    })
                    .onCancel(p -> Homestead.getInstance().runSyncTask(() -> new RentConfigMenu(player, region, subArea)))
                    .build();
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleNoticeToVacate(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

            if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_SUBAREAS, true)) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            SeRent rent = subArea != null ? subArea.getRent() : region.getRent();

            if (!rent.hasRenter()) {
                Messages.send(player, "commands.rent.3");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            if (rent.isPermanent()) {
                Messages.send(player, "commands.rent.4");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            if (rent.hasNoticeToVacate()) {
                rent.setNoticeToVacate(null);
                rent.setUntilAt(rent.getStartedAt() + rent.getDuration());
                Homestead.callEvent(new RentNoticeEvent(region, subArea, rent, true));
                Messages.send(player, "commands.rent.5");
                PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
                new RentConfigMenu(player, region, subArea);
                return;
            }

            player.closeInventory();

            RegionsFile config = Resources.get(ResourceType.Regions);
            int noticeDays = config.getNoticeToVacateDays();

            PlayerInputSession.builder(Homestead.getInstance(), player)
                    .prompt(13)
                    .validator(msg -> validateNoticeDays(player, msg, 1, 30))
                    .callback((p, input) -> {
                        int days = input.isEmpty() ? noticeDays : Integer.parseInt(input);
                        long noticeAt = System.currentTimeMillis();
                        long vacateAt = noticeAt + (days * 24L * 60 * 60 * 1000);

                        if (vacateAt > rent.getUntilAt()) {
                            vacateAt = rent.getUntilAt();
                        }

                        rent.setNoticeToVacate(new SeNoticeToVacate(noticeAt, days));
                        rent.setUntilAt(vacateAt);
                        Homestead.callEvent(new RentNoticeEvent(region, subArea, rent, false));
                        Messages.send(player, "commands.rent.6", days);
                        PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
                        Homestead.getInstance().runSyncTask(() -> new RentConfigMenu(player, region, subArea));
                    })
                    .onCancel(p -> Homestead.getInstance().runSyncTask(() -> new RentConfigMenu(player, region, subArea)))
                    .build();
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleCancelRent(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

            if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_SUBAREAS, true)) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            SeRent rent = subArea != null ? subArea.getRent() : region.getRent();

            if (!rent.hasRenter()) {
                Messages.send(player, "commands.rent.0");
                return;
            }

            OfflinePlayer renter = rent.getRenter();

            if (renter == null) return;

            double deposit = rent.getSecurityDeposit();

            RentEndEvent endEvent = new RentEndEvent(region, subArea, renter, rent);
            Homestead.callEvent(endEvent);
            if (endEvent.isCancelled()) return;

            rent.clearRenter();

            if (deposit > 0) {
                PlayerBank.deposit(renter, deposit);
            }

            Messages.send(player, "commands.rent.7");
            PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
            new RentConfigMenu(player, region, subArea);
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleEndRent(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

            if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_SUBAREAS, true) && !PlayerUtility.isOperator(player) && !region.isOwner(player)) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            SeRent rent = subArea != null ? subArea.getRent() : region.getRent();

            if (!rent.hasRenter()) {
                Messages.send(player, "commands.rent.0");
                return;
            }

            OfflinePlayer renter = rent.getRenter();

            if (renter == null) return;

            double deposit = rent.getSecurityDeposit();

            RentEndEvent endEvent = new RentEndEvent(region, subArea, renter, rent);
            Homestead.callEvent(endEvent);
            if (endEvent.isCancelled()) return;

            rent.clearRenter();

            if (deposit > 0) {
                PlayerBank.deposit(renter, deposit);
            }

            Messages.send(player, "commands.rent.7");
            PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
            new RentConfigMenu(player, region, subArea);
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleSetPermanent(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (!checkValid(player, region, subArea) || !event.isLeftClick()) return;

            if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.MANAGE_SUBAREAS, true)) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            SeRent rent = subArea != null ? subArea.getRent() : region.getRent();

            if (rent.hasRenter()) {
                Messages.send(player, "commands.rent.16");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            long oldDuration = rent.getDuration();
            rent.setDuration(-1L);
            rent.setUntilAt(-1L);
            rent.setNoticeToVacate(null);

            Homestead.callEvent(new RentConfigUpdateEvent(region, subArea, rent, "duration", oldDuration, -1L));

            Messages.send(player, "commands.rent.8");
            PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
            new RentConfigMenu(player, region, subArea);
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleBack(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (checkValid(player, region, subArea) && event.isLeftClick()) {
                if (subArea != null) {
                    new SubAreaMenu(player, region, subArea);
                } else {
                    new SelectedRegionMainMenu(player, region);
                }
            }
        };
    }

    private static boolean validatePrice(Player player, String input, double min, double max) {
        try {
            double value = Double.parseDouble(input);
            if (value < min || value > max) {
                Messages.send(player, "commands.rent.9", Formatter.getBalance(min), Formatter.getBalance(max));
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            Messages.send(player, "commands.rent.10");
            return false;
        }
    }

    private static boolean validateDuration(Player player, String input, int minDays, int maxDays) {
        try {
            long days = Long.parseLong(input);
            if (days != -1 && (days < minDays || days > maxDays)) {
                Messages.send(player, "commands.rent.11", minDays, maxDays);
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            Messages.send(player, "commands.rent.10");
            return false;
        }
    }

    private static boolean validateNoticeDays(Player player, String input, int min, int max) {
        if (input.isEmpty()) return true;
        try {
            int value = Integer.parseInt(input);
            if (value < min || value > max) {
                Messages.send(player, "commands.rent.12", min, max);
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            Messages.send(player, "commands.rent.10");
            return false;
        }
    }

    private static boolean checkValid(Player player, Region region, SubArea subArea) {
        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return false;
        }
        if (subArea != null && SubAreaManager.findSubArea(subArea.getUniqueId()) == null) {
            player.closeInventory();
            return false;
        }
        return true;
    }
}
