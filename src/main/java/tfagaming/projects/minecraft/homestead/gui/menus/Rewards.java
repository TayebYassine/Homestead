package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;

public final class Rewards {
	public Rewards(Player player, Region region, Runnable backButton) {
		Menu gui = new Menu(MenuUtility.getTitle(23).replace("{region}", region.getName()), 9 * 3);

		gui.addItem(12, MenuUtility.getButton(66, new Placeholder()
				.add("{region}", region.getName())
				.add("{members}", region.getMembers().size())
				.add("{chunks}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getChunksByEachMember(region))
				.add("{subareas}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getSubAreasByEachMember(region))
		), null);

		gui.addItem(14, MenuUtility.getButton(67, new Placeholder()
				.add("{region}", region.getName())
				.add("{members}", region.getMembers().size())
				.add("{player-playtime}", Formatter.getPlayerPlaytime(player))
				.add("{chunks}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getChunksByPlayTime(player))
				.add("{subareas}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getSubAreasByPlayTime(player))
		), null);

		gui.addItem(18, MenuUtility.getBackButton(), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			backButton.run();
		});

		gui.open(player, MenuUtility.getEmptySlot());
	}
}