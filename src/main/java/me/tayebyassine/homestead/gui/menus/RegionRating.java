package me.tayebyassine.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.managers.RateManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.tools.java.Formatter;
import me.tayebyassine.homestead.tools.java.Placeholder;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;
import me.tayebyassine.homestead.tools.minecraft.menus.MenuUtility;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerSound;

import java.util.function.BiConsumer;

public final class RegionRating {
	private static final int[] RATING_SLOTS = {11, 12, 13, 14, 15};
	private static final int[] RATING_BUTTON_IDS = {48, 49, 50, 51, 52};

	public RegionRating(Player player, Region region, Runnable backButton) {
		Placeholder placeholder = new Placeholder()
				.add("{region}", region.getName())
				.add("{player-rate}", RateManager.hasRatedRegion(player, region)
						? Formatter.getRating(RateManager.getPlayerRate(player, region).getRate())
						: Formatter.getNone());

		Menu.Builder<?> builder = Menu.builder(MenuUtility.getTitle(17).replace("{region}", region.getName()), 9 * 3);

		for (int i = 0; i < RATING_SLOTS.length; i++) {
			final int rating = i + 1;
			builder.button(RATING_SLOTS[i], MenuUtility.getButton(RATING_BUTTON_IDS[i], placeholder),
					handleRate(player, region, rating, backButton));
		}

		builder.item(22, RateManager.hasRatedRegion(player, region)
						? MenuUtility.getButton(54, placeholder)
						: MenuUtility.getButton(53))
				.button(18, MenuUtility.getBackButton(), handleBack(player, region, backButton))
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