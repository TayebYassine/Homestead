package me.tayebyassine.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.menus.MenuUtility;

import java.util.function.BiConsumer;

public final class Rewards {
	public Rewards(Player player, Region region, Runnable backButton) {
		Menu.builder(MenuUtility.getTitle(23).replace("{region}", region.getName()), 9 * 3)
				.item(12, MenuUtility.getButton(66, new Placeholder()
						.add("{region}", region.getName())
						.add("{members}", MemberManager.getMembersOfRegion(region).size())
						.add("{chunks}", me.tayebyassine.homestead.util.minecraft.rewards.Rewards.getChunksByEachMember(region))
						.add("{subareas}", me.tayebyassine.homestead.util.minecraft.rewards.Rewards.getSubAreasByEachMember(region))))
				.item(14, MenuUtility.getButton(67, new Placeholder()
						.add("{region}", region.getName())
						.add("{members}", MemberManager.getMembersOfRegion(region).size())
						.add("{player-playtime}", Formatter.getPlayerPlaytime(player))
						.add("{chunks}", me.tayebyassine.homestead.util.minecraft.rewards.Rewards.getChunksByPlayTime(player))
						.add("{subareas}", me.tayebyassine.homestead.util.minecraft.rewards.Rewards.getSubAreasByPlayTime(player))))
				.button(18, MenuUtility.getBackButton(), handleBack(player, region, backButton))
				.fillEmptySlots()
				.build()
				.open(player);
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