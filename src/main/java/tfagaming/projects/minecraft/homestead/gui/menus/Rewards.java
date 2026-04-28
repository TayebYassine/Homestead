package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;

import java.util.function.BiConsumer;

public final class Rewards {
	public Rewards(Player player, Region region, Runnable backButton) {
		Menu.builder(MenuUtility.getTitle(23).replace("{region}", region.getName()), 9 * 3)
				.item(12, MenuUtility.getButton(66, new Placeholder()
						.add("{region}", region.getName())
						.add("{members}", MemberManager.getMembersOfRegion(region).size())
						.add("{chunks}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getChunksByEachMember(region))
						.add("{subareas}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getSubAreasByEachMember(region))))
				.item(14, MenuUtility.getButton(67, new Placeholder()
						.add("{region}", region.getName())
						.add("{members}", MemberManager.getMembersOfRegion(region).size())
						.add("{player-playtime}", Formatter.getPlayerPlaytime(player))
						.add("{chunks}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getChunksByPlayTime(player))
						.add("{subareas}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getSubAreasByPlayTime(player))))
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