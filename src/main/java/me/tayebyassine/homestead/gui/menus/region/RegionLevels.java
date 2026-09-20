package me.tayebyassine.homestead.gui.menus.region;

import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.gui.helpers.MenuButtons;
import me.tayebyassine.homestead.managers.LevelManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Level;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.MenusFile;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.NumberUtils;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.rewards.LevelRewards;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginated region levels display showing all level tiers and the region's current progress.
 */
public final class RegionLevels {
    private static final String MENU_KEY = "region_levels";
    private static final int MAX_LEVEL = 50;

    public RegionLevels(Player player, Region region, Runnable backButton) {
        Level lvl = LevelManager.getLevelByRegion(region.getUniqueId());

        PaginationMenu gui = PaginationMenu.builder(MENU_KEY, new Placeholder().add("{region}", region.getName()), 9 * 5)
                .nextPageItem(MenuButtons.getNextPageButton())
                .prevPageItem(MenuButtons.getPreviousPageButton())
                .items(buildLevelButtons(region))
                .itemsPerPage(9)
                .fillEmptySlots()
                .goBack((p, e) -> backButton.run())
                .onClick((p, c) -> {
                    if (RegionManager.findRegion(region.getUniqueId()) == null) {
                        player.closeInventory();
                    }
                })
                .actionButton(1, MenuButtons.getButton(MENU_KEY, 0, new Placeholder()
                        .add("{level}", lvl == null ? 0 : lvl.getLevel())
                        .add("{xp}", NumberUtils.convertToBalance(lvl == null ? 0 : lvl.getExperience()))
                        .add("{reward-chunks}", LevelRewards.getChunksByLevel(region))
                        .add("{reward-members}", LevelRewards.getMembersByLevel(region))
                        .add("{reward-subareas}", LevelRewards.getSubAreasByLevel(region))
                        .add("{reward-upkeep}", LevelRewards.getUpkeepReductionByLevel(region))), null)
                .onOpen(inv -> buildXpBar(inv, lvl))
                .build();

        gui.open(player);
    }

    private void buildXpBar(Inventory inv, Level lvl) {
        ItemStack empty = MenuButtons.getEmptySlot();
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

        ItemStack bluePane = MenuButtons.getButton(MENU_KEY, 1, placeholder);
        ItemStack grayPane = MenuButtons.getButton(MENU_KEY, 2, placeholder);

        for (int i = 0; i < blue; i++) inv.setItem(27 + i, bluePane);
        for (int i = 0; i < gray; i++) inv.setItem(27 + blue + i, grayPane);
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

            list.add(MenuButtons.getButton(MENU_KEY, l <= unlocked ? 3 : 4, placeholder));
        }

        return list;
    }

    private String getLevelRewardInfo(int lvl) {
        List<String> rewards = Resources.<MenusFile>get(ResourceType.Menus).getStringList("button-levels." + lvl);

        if (rewards == null || rewards.isEmpty()) return Formatter.getNone();

        return String.join("\n", rewards);
    }
}