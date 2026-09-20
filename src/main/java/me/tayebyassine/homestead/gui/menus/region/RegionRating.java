package me.tayebyassine.homestead.gui.menus.region;

import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.managers.RateManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.BiConsumer;

/**
 * Region rating menu allowing players to rate a region from 1 to 5 stars.
 */
public final class RegionRating {
    private static final String MENU_KEY = "region_rating";
    private static final int[] RATING_SLOTS = {11, 12, 13, 14, 15};
    private static final int[] RATING_BUTTON_IDS = {0, 1, 2, 3, 4};

    public RegionRating(Player player, Region region, Runnable backButton) {
        Placeholder placeholder = new Placeholder()
                .add("{region}", region.getName())
                .add("{player-rate}", RateManager.hasRatedRegion(player, region)
                        ? Formatter.getRating(RateManager.getPlayerRate(player, region).getRate())
                        : Formatter.getNone());

        Menu.Builder<?> builder = Menu.builder(MENU_KEY, new Placeholder().add("{region}", region.getName()), 9 * 3);

        for (int i = 0; i < RATING_SLOTS.length; i++) {
            final int rating = i + 1;
            builder.button(RATING_SLOTS[i], MenuButtons.getButton(MENU_KEY, RATING_BUTTON_IDS[i], placeholder),
                    handleRate(player, region, rating, backButton));
        }

        builder.item(22, RateManager.hasRatedRegion(player, region)
                        ? MenuButtons.getButton(MENU_KEY, 6, placeholder)
                        : MenuButtons.getButton(MENU_KEY, 5))
                .button(18, MenuButtons.getBackButton(), handleBack(player, region, backButton))
                .fillEmptySlots()
                .build()
                .open(player);
    }

    private static BiConsumer<Player, InventoryClickEvent> handleRate(Player player, Region region, int rating, Runnable backButton) {
        return (_player, event) -> {
            if (RegionManager.findRegion(region.getUniqueId()) == null) {
                player.closeInventory();
                return;
            }

            if (!player.hasPermission("homestead.actions.regions.rate")) {
                Messages.send(player, "common.no_permission");
                PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
                return;
            }

            if (!event.isLeftClick()) return;

            RateManager.rateRegion(region, player, rating);

            PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

            new RegionRating(player, region, backButton);
        };
    }

    private static BiConsumer<Player, InventoryClickEvent> handleBack(Player player, Region region, Runnable backButton) {
        return (_player, event) -> {
            if (RegionManager.findRegion(region.getUniqueId()) == null) {
                player.closeInventory();
                return;
            }
            if (!event.isLeftClick()) return;
            backButton.run();
        };
    }
}