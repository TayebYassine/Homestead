package me.tayebyassine.homestead.gui.menus;

import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.menus.MenuUtility;
import me.tayebyassine.homestead.util.minecraft.players.PlayerBank;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.BiConsumer;

public final class RentConfirmationMenu {
    public RentConfirmationMenu(Player player, Region region, SubArea subArea, SeRent rentOffer) {
        Placeholder placeholder = new Placeholder()
                .add("{region}", region.getName())
                .add("{subarea}", subArea != null ? subArea.getName() : Formatter.getNA())
                .add("{rent-price}", Formatter.getBalance(rentOffer.getPrice()))
                .add("{rent-duration}", Formatter.getDuration(rentOffer.getDuration()))
                .add("{rent-deposit}", rentOffer.getSecurityDeposit() > 0 ? Formatter.getBalance(rentOffer.getSecurityDeposit()) : Formatter.getNone())
                .add("{rent-total}", Formatter.getBalance(rentOffer.getPrice() + rentOffer.getSecurityDeposit()))
                .add("{player-balance}", Formatter.getBalance(PlayerBank.get(player)));

        String title = MenuUtility.getTitle(33).replace("{region-or-subarea}", subArea != null ? subArea.getName() : region.getName());

        Menu.builder(title, 9 * 3)
                .item(13, MenuUtility.getButton(92, placeholder))
                .button(11, MenuUtility.getButton(93, placeholder), handleAccept(player, region, subArea, rentOffer))
                .button(15, MenuUtility.getButton(94, placeholder), handleDeny(player, region, subArea))
                .fillEmptySlots()
                .build()
                .open(player);
    }

    private static BiConsumer<Player, InventoryClickEvent> handleAccept(Player player, Region region, SubArea subArea, SeRent rentOffer) {
        return (_player, event) -> {
            if (!event.isLeftClick()) return;

            if (subArea != null) {
                SeRent currentRent = subArea.getRent();
                if (currentRent.hasRenter()) {
                    Messages.send(player, "commands.rent.13");
                    PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                    return;
                }
            } else {
                SeRent currentRent = region.getRent();
                if (currentRent.hasRenter()) {
                    Messages.send(player, "commands.rent.13");
                    PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                    return;
                }
            }

            double totalCost = rentOffer.getPrice() + rentOffer.getSecurityDeposit();
            if (totalCost > PlayerBank.get(player)) {
                Messages.send(player, "signs.4");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            PlayerBank.withdraw(player, totalCost);
            PlayerBank.deposit(region.getOwner(), rentOffer.getPrice());

            long startedAt = System.currentTimeMillis();
            long untilAt = rentOffer.isPermanent() ? -1L : startedAt + rentOffer.getDuration();

            SeRent newRent = new SeRent(
                    player.getUniqueId(),
                    rentOffer.getDuration(),
                    startedAt,
                    untilAt,
                    rentOffer.getSecurityDeposit(),
                    rentOffer.getPrice()
            );

            if (subArea != null) {
                subArea.setRent(newRent);
                Messages.send(player, "signs.9", untilAt > 0 ? Formatter.getRemainingTime(untilAt) : Formatter.getNever());
            } else {
                region.setRent(newRent);
                Messages.send(player, "signs.8", untilAt > 0 ? Formatter.getRemainingTime(untilAt) : Formatter.getNever());
            }

            player.closeInventory();

            PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleDeny(Player player, Region region, SubArea subArea) {
        return (_player, event) -> {
            if (!event.isLeftClick()) return;

            player.closeInventory();

            Messages.send(player, "commands.rent.14");
            PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
        };
    }
}