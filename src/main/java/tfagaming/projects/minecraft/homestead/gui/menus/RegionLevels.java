package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.LevelManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Level;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.MenusFile;


import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.LevelRewards;

import java.util.ArrayList;
import java.util.List;

public final class RegionLevels {
	private static final int MAX_LEVEL = 50;

	public RegionLevels(Player player, Region region, Runnable backButton) {
		Level lvl = LevelManager.getLevelByRegion(region.getUniqueId());

		PaginationMenu gui = new PaginationMenu(
				MenuUtility.getTitle(26).replace("{region}", region.getName()),
				9 * 5,
				MenuUtility.getNextPageButton(),
				MenuUtility.getPreviousPageButton(),
				buildLevelButtons(region),
				(p, e) -> backButton.run(),
				(p, c) -> {
					if (RegionManager.findRegion(region.getUniqueId()) == null) {
						player.closeInventory();
					}
				}
		);

		gui.setItemsPerPage(9);

		gui.addOpenHandler(inv -> {
			ItemStack empty = MenuUtility.getEmptySlot();
			for (int i = 18; i < 27; i++) inv.setItem(i, empty);

			int current = lvl == null ? 0 : lvl.getLevel();
			long xp = lvl == null ? 0 : lvl.getExperience();
			double percentage = lvl == null ? 0.0 : lvl.getProgressPercentage();
			long needed = Level.getXpForLevel(current);
			double pct = needed == 0 ? 0 : (double) xp / needed;

			int blue = (int) Math.round(9 * pct);
			int gray = 9 - blue;

			Placeholder placeholder = new Placeholder()
					.add("{level}", current)
					.add("{next-lvl}", current + 1)
					.add("{xp}", NumberUtils.convertToBalance(xp))
					.add("{next-lvl-xp}", NumberUtils.convertToBalance(needed))
					.add("{next-lvl-percentage}", NumberUtils.truncate(percentage));

			ItemStack bluePane = MenuUtility.getButton(75, placeholder);
			ItemStack grayPane = MenuUtility.getButton(76, placeholder);

			for (int i = 0; i < blue; i++) inv.setItem(27 + i, bluePane);
			for (int i = 0; i < gray; i++) inv.setItem(27 + blue + i, grayPane);
		});

		gui.addActionButton(1, MenuUtility.getButton(74, new Placeholder()
				.add("{level}", lvl == null ? 0 : lvl.getLevel())
				.add("{xp}", NumberUtils.convertToBalance(lvl == null ? 0 : lvl.getExperience()))
				.add("{reward-chunks}", LevelRewards.getChunksByLevel(region))
				.add("{reward-members}", LevelRewards.getMembersByLevel(region))
				.add("{reward-subareas}", LevelRewards.getSubAreasByLevel(region))
				.add("{reward-upkeep}", LevelRewards.getUpkeepReductionByLevel(region))
		), (_a, _c) -> {
		});

		gui.open(player, MenuUtility.getEmptySlot());
	}

	private List<ItemStack> buildLevelButtons(Region region) {
		Level lvl = LevelManager.getLevelByRegion(region.getUniqueId());
		int unlocked = lvl == null ? 0 : lvl.getLevel();
		long currentXp = lvl == null ? 0 : lvl.getExperience();

		List<ItemStack> list = new ArrayList<>();

		for (int l = 0; l <= MAX_LEVEL; l++) {
			Placeholder placeholder = new Placeholder()
					.add("{level}", l)
					.add("{xp}", NumberUtils.convertToBalance(Level.getXpForLevel(l)))
					.add("{current-xp}", NumberUtils.convertToBalance(currentXp))
					.add("{level-rewards}", getLevelRewardInfo(l));

			list.add(MenuUtility.getButton(l <= unlocked ? 77 : 78, placeholder));
		}

		return list;
	}

	private String getLevelRewardInfo(int lvl) {
		List<String> rewards = Resources.<MenusFile>get(ResourceType.Menus).getStringList("button-levels." + lvl);

		if (rewards == null || rewards.isEmpty()) return Formatter.getNone();

		return String.join("\n", rewards);
	}
}