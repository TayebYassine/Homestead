package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;

public final class RegionRating {
	/** Slot and button-ID arrays for the 5 rating options (rating 1–5). */
	private static final int[] RATING_SLOTS = {11, 12, 13, 14, 15};
	private static final int[] RATING_BUTTON_IDS = {48, 49, 50, 51, 52};

	public RegionRating(Player player, Region region, Runnable backButton) {
		Menu gui = new Menu(MenuUtils.getTitle(17).replace("{region}", region.getName()), 9 * 3);

		Placeholder placeholder = new Placeholder()
				.add("{region}", region.getName())
				.add("{player-rate}", region.isPlayerRated(player)
						? Formatter.getRating(region.getPlayerRate(player).getRate())
						: Formatter.getNone());

		for (int i = 0; i < RATING_SLOTS.length; i++) {
			final int rating = i + 1;

			gui.addItem(RATING_SLOTS[i], MenuUtils.getButton(RATING_BUTTON_IDS[i], placeholder), (_player, event) -> {
				if (RegionManager.findRegion(region.getUniqueId()) == null) {
					player.closeInventory();
					return;
				}

				if (!event.isLeftClick()) return;

				region.addPlayerRate(player, rating);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				Messages.send(player, 134);
				new RegionRating(player, region, backButton);
			});
		}

		gui.addItem(22, region.isPlayerRated(player)
						? MenuUtils.getButton(54, placeholder)
						: MenuUtils.getButton(53), null);

		gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			backButton.run();
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}
}