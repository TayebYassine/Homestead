package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;

public class Rewards {
	public Rewards(Player player, Region region, Runnable backButton) {
		Menu gui = new Menu(MenuUtils.getTitle(23).replace("{region}", region.getName()), 9 * 3);

		gui.addItem(12, MenuUtils.getButton(66, new Placeholder()
				.add("{region}", region.getName())
				.add("{members}", region.getMembers().size())
				.add("{chunks}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getChunksByEachMember(region))
				.add("{subareas}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getSubAreasByEachMember(region))
		), null);

		gui.addItem(14, MenuUtils.getButton(67, new Placeholder()
				.add("{region}", region.getName())
				.add("{members}", region.getMembers().size())
				.add("{player-playtime}", Formatter.getPlayerPlaytime(player))
				.add("{chunks}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getChunksByPlayTime(player))
				.add("{subareas}", tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards.getSubAreasByPlayTime(player))
		), null);

		gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
			if (!event.isLeftClick()) return;
			backButton.run();
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}
}