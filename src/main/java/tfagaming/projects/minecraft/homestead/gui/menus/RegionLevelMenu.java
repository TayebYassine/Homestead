package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.LevelsManager;
import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;

import java.util.ArrayList;
import java.util.List;

public class RegionLevelMenu {

	private static final int MAX_LEVEL = 100;

	public RegionLevelMenu(Player player, Region region) {
		List<ItemStack> levelButtons = buildLevelButtons(region);

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(26).replace("{region}", region.getName()),
				9 * 5,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				levelButtons,
				(p, e) -> {
					player.closeInventory();
				},
				(p, c) -> { }
		);

		gui.setItemsPerPage(9);

		gui.addOpenHandler(inv -> {
			ItemStack empty = MenuUtils.getEmptySlot();

			/* row 3  (slots 18-26) */
			for (int i = 18; i < 27; i++) inv.setItem(i, empty);

			/* row 4  (slots 27-35) – progress bar */
			Level lvl = LevelsManager.getLevelByRegion(region.getUniqueId());
			int current = lvl == null ? 0 : lvl.getLevel();
			long xp = lvl == null ? 0 : lvl.getExperience();
			long needed = Level.getXpForLevel(current);
			double pct = needed == 0 ? 0 : (double) xp / needed;
			int blue = (int) Math.round(9 * pct);
			int gray = 9 - blue;

			ItemStack bluePane = createPane(Material.BLUE_STAINED_GLASS_PANE, "§bProgress");
			ItemStack grayPane = createPane(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§7Remaining");

			int start = 27;
			for (int i = 0; i < blue; i++) inv.setItem(start + i, bluePane);
			for (int i = 0; i < gray; i++) inv.setItem(start + blue + i, grayPane);
		});

		gui.addActionButton(1, MenuUtils.getButton(74), (_a, _c) -> {

		});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	private List<ItemStack> buildLevelButtons(Region region) {
		List<ItemStack> list = new ArrayList<>();
		Level lvl = LevelsManager.getLevelByRegion(region.getUniqueId());
		int unlocked = lvl == null ? 0 : lvl.getLevel();

		for (int l = 0; l <= MAX_LEVEL; l++) {
			boolean isUnlocked = l <= unlocked;
			ItemStack icon = MenuUtils.getButton(isUnlocked ? 77 : 78);
			ItemMeta meta = icon.getItemMeta();
			if (meta != null) {
				meta.setDisplayName("§eLevel " + l);
				meta.setLore(List.of(isUnlocked ? "§a✔ Unlocked" : "§c✖ Locked"));
				icon.setItemMeta(meta);
			}
			list.add(icon);
		}
		return list;
	}

	private ItemStack createPane(Material mat, String name) {
		ItemStack pane = new ItemStack(mat);
		ItemMeta meta = pane.getItemMeta();
		if (meta != null) meta.setDisplayName(name);
		pane.setItemMeta(meta);
		return pane;
	}
}