package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.LevelsManager;
import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.LevelRewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionLevelMenu {
	private static final int MAX_LEVEL = 50;

	public RegionLevelMenu(Player player, Region region, Runnable backButton) {
		Level lvl = LevelsManager.getLevelByRegion(region.getUniqueId());

		List<ItemStack> levelButtons = buildLevelButtons(region);

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(26).replace("{region}", region.getName()),
				9 * 5,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				levelButtons,
				(p, e) -> {
					backButton.run();
				},
				(p, c) -> { }
		);

		gui.setItemsPerPage(9);

		gui.addOpenHandler(inv -> {
			ItemStack empty = MenuUtils.getEmptySlot();

			for (int i = 18; i < 27; i++) inv.setItem(i, empty);

			int current = lvl == null ? 0 : lvl.getLevel();
			long xp = lvl == null ? 0 : lvl.getExperience();
			long needed = Level.getXpForLevel(current);
			double pct = needed == 0 ? 0 : (double) xp / needed;
			int blue = (int) Math.round(9 * pct);
			int gray = 9 - blue;

			Map<String, String> replacements = new HashMap<>();

			replacements.put("{level}", String.valueOf(current));
			replacements.put("{next-lvl}", String.valueOf(current + 1));
			replacements.put("{xp}", NumberUtils.convertToBalance(xp));
			replacements.put("{next-lvl-xp}", NumberUtils.convertToBalance(needed));

			ItemStack bluePane = MenuUtils.getButton(75, replacements);
			ItemStack grayPane = MenuUtils.getButton(76, replacements);

			int start = 27;
			for (int i = 0; i < blue; i++) inv.setItem(start + i, bluePane);
			for (int i = 0; i < gray; i++) inv.setItem(start + blue + i, grayPane);
		});

		Map<String, String> replacements = new HashMap<>();
		replacements.put("{level}", String.valueOf(lvl == null ? 0 : lvl.getLevel()));
		replacements.put("{xp}", NumberUtils.convertToBalance(lvl == null ? 0 : lvl.getExperience()));
		replacements.put("{reward-chunks}", String.valueOf(LevelRewards.getChunksByLevel(region)));
		replacements.put("{reward-members}", String.valueOf(LevelRewards.getMembersByLevel(region)));
		replacements.put("{reward-subareas}", String.valueOf(LevelRewards.getSubAreasByLevel(region)));
		replacements.put("{reward-upkeep}", String.valueOf(LevelRewards.getUpkeepReductionByLevel(region)));

		gui.addActionButton(1, MenuUtils.getButton(74, replacements), (_a, _c) -> {

		});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	private List<ItemStack> buildLevelButtons(Region region) {
		List<ItemStack> list = new ArrayList<>();
		Level lvl = LevelsManager.getLevelByRegion(region.getUniqueId());
		int unlocked = lvl == null ? 0 : lvl.getLevel();

		for (int l = 0; l <= MAX_LEVEL; l++) {
			boolean isUnlocked = l <= unlocked;

			Map<String, String> replacements = new HashMap<>();
			replacements.put("{level}", String.valueOf(l));
			replacements.put("{xp}", NumberUtils.convertToBalance(Level.getXpForLevel(l)));
			replacements.put("{current-xp}", NumberUtils.convertToBalance(lvl == null ? 0 : lvl.getExperience()));
			replacements.put("{level-rewards}", getLevelRewardInfo(l));

			ItemStack icon = MenuUtils.getButton(isUnlocked ? 77 : 78, replacements);

			list.add(icon);
		}
		return list;
	}

	private String getLevelRewardInfo(int lvl) {
		List<String> rewards = Homestead.menusConfig.get("button-levels." + lvl);

		if (rewards == null || rewards.isEmpty()) {
			return Formatters.getNone();
		}

		return String.join("\n",  rewards);
	}
}