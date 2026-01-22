package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerRewards;

import java.util.HashMap;

public class RewardsMenu {
	public RewardsMenu(Player player, Region region, Runnable backButton) {
		Menu gui = new Menu(MenuUtils.getTitle(23).replace("{region}", region.getName()), 9 * 3);

		HashMap<String, String> replacements = new HashMap<>();
		replacements.put("{region}", region.getName());
		replacements.put("{members}", String.valueOf(region.getMembers().size()));

		replacements.put("{chunks}", String.valueOf(PlayerRewards.getChunksByEachMember(player)));
		replacements.put("{subareas}", String.valueOf(PlayerRewards.getSubAreasByEachMember(player)));
		ItemStack membersRewardButton = MenuUtils.getButton(66, replacements);

		gui.addItem(12, membersRewardButton, (_player, event) -> {
			// Do nothing
		});

		replacements.put("{chunks}", String.valueOf(PlayerRewards.getChunksByPlayTime(player)));
		replacements.put("{subareas}", String.valueOf(PlayerRewards.getSubAreasByPlayTime(player)));
		ItemStack playtimeRewardButton = MenuUtils.getButton(67, replacements);

		gui.addItem(14, playtimeRewardButton, (_player, event) -> {
			// Do nothing
		});

		gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
			if (!event.isLeftClick()) {
				return;
			}

			backButton.run();
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}
}
