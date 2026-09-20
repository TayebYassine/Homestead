package me.tayebyassine.homestead.gui.menus.region;

import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.managers.RateManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.serialize.SeLocation;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.ListUtils;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.DelayedTeleport;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginated list of regions with welcome signs.
 *
 * <p>Allows players to browse and teleport to regions that have set a welcome sign location.</p>
 */
public final class RegionsWithWelcomeSignsMenu {
    private static final String MENU_KEY = "regions_with_welcome_signs";

    private final List<Region> regions;

    public RegionsWithWelcomeSignsMenu(Player player) {
        this.regions = ListUtils.removeDuplications(
                new ArrayList<>(RegionManager.getRegionsWithWelcomeSigns()));

        PaginationMenu.builder(MENU_KEY, 9 * 5)
                .nextPageItem(MenuButtons.getNextPageButton())
                .prevPageItem(MenuButtons.getPreviousPageButton())
                .items(getItems(player))
                .fillEmptySlots()
                .goBack((_player, event) -> _player.closeInventory())
                .onClick((_player, context) -> handleTeleport(player, context))
                .build()
                .open(player);
    }

    private void handleTeleport(Player player, PaginationMenu.ClickContext context) {
        if (context.index() >= regions.size()) return;

        Region region = regions.get(context.index());

        if (!context.event().isLeftClick()) return;

        if (RegionManager.findRegion(region.getUniqueId()) == null) {
            player.closeInventory();
            return;
        }

        if (!player.hasPermission("homestead.actions.regions.teleport")) {
            Messages.send(player, "common.no_permission");
            PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
            return;
        }

        SeLocation location = region.getWelcomeSign();
        if (location == null) return;
        Location bukkitLocation = location.toBukkit();
        if (bukkitLocation == null) return;

        player.closeInventory();
        new DelayedTeleport(player, bukkitLocation);
    }

    private List<ItemStack> getItems(Player player) {
        List<ItemStack> items = new ArrayList<>();

        for (Region region : regions) {
            items.add(MenuButtons.getButton(MENU_KEY, 0, new Placeholder()
                    .add("{region}", region.getName())
                    .add("{region-displayname}", region.getDisplayName())
                    .add("{region-owner}", region.getOwnerName())
                    .add("{region-bank}", Formatter.getBalance(region.getBank()))
                    .add("{region-createdat}", Formatter.getDate(region.getCreatedAt()))
                    .add("{region-rating}", Formatter.getRating(RateManager.getAverageRating(region)))));
        }

        return items;
    }
}
