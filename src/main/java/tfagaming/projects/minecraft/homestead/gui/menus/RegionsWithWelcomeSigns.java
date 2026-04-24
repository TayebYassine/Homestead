package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.DelayedTeleport;

import java.util.ArrayList;
import java.util.List;

public final class RegionsWithWelcomeSigns {
	private final List<Region> regions;

	public RegionsWithWelcomeSigns(Player player) {
		regions = ListUtils.removeDuplications(
				new ArrayList<>(RegionManager.getRegionsWithWelcomeSigns()));

		PaginationMenu gui = new PaginationMenu(
				MenuUtility.getTitle(0), 9 * 5,
				MenuUtility.getNextPageButton(),
				MenuUtility.getPreviousPageButton(),
				getItems(player),
				(_player, event) -> _player.closeInventory(),
				(_player, context) -> {
					if (context.getIndex() >= regions.size()) return;

					Region region = regions.get(context.getIndex());

					if (context.getEvent().isLeftClick()) {
						if (RegionManager.findRegion(region.getUniqueId()) == null) {
							player.closeInventory();
							return;
						}

						if (!player.hasPermission("homestead.region.teleport")) {
							Messages.send(player, 212);
							return;
						}

						player.closeInventory();

						new DelayedTeleport(player, region.getWelcomeSign().bukkit());
					}
				});

		gui.open(player, MenuUtility.getEmptySlot());
	}

	private List<ItemStack> getItems(Player player) {
		List<ItemStack> items = new ArrayList<>();

		for (Region region : regions) {
			items.add(MenuUtility.getButton(47, new Placeholder()
					.add("{region}", region.getName())
					.add("{region-displayname}", region.getDisplayName())
					.add("{region-owner}", region.getOwner().getName())
					.add("{region-bank}", Formatter.getBalance(region.getBank()))
					.add("{region-createdat}", Formatter.getDate(region.getCreatedAt()))
					.add("{region-rating}", Formatter.getRating(RegionManager.getAverageRating(region)))));
		}

		return items;
	}
}