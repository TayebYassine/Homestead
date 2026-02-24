package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopRegionsMenu {
	private final SortMode sortMode;
	private final boolean isPublicRegionsOnly;
	private List<Region> regions;

	public TopRegionsMenu(Player player, boolean isPublic) {
		this(player, isPublic, SortMode.BANK);
	}

	public TopRegionsMenu(Player player, boolean isPublic, SortMode sortMode) {
		this.isPublicRegionsOnly = isPublic;
		this.sortMode = sortMode;

		regions = new ArrayList<>(RegionsManager.sortRegions(sortMode.sorting));
		regions = ListUtils.removeDuplications(regions);

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(sortMode.titleId),
				9 * 5,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				getItems(player),
				(_player, event) -> _player.closeInventory(),
				(_player, context) -> {
					if (context.getIndex() >= regions.size()) return;

					Region region = regions.get(context.getIndex());

					if (context.getEvent().isLeftClick()) {
						new RegionInfoMenu(player, region, () ->
								new TopRegionsMenu(player, this.isPublicRegionsOnly, sortMode));
					}
				}
		);

		gui.addActionButton(0, MenuUtils.getButton(sortMode.buttonId), (_player, event) -> {
			if (event.isLeftClick()) {
				new TopRegionsMenu(player, this.isPublicRegionsOnly, sortMode.next());
			} else if (event.isRightClick()) {
				new TopRegionsMenu(player, this.isPublicRegionsOnly, sortMode.previous());
			}

			PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
		});

		Map<String, String> replacements = new HashMap<>();
		replacements.put("{is-public-regions}", Formatter.getToggle(this.isPublicRegionsOnly));

		gui.addActionButton(2, MenuUtils.getButton(81, replacements), (_player, event) -> {
			if (event.isLeftClick()) {
				new TopRegionsMenu(player, !this.isPublicRegionsOnly, sortMode);
			}
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	private List<ItemStack> getItems(Player player) {
		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < regions.size(); i++) {
			Region region = regions.get(i);

			if (this.isPublicRegionsOnly && !region.isPublic()) continue;

			HashMap<String, String> replacements = new HashMap<>();
			replacements.put("{rank}", String.valueOf(i + 1));
			replacements.put("{region}", region.getName());
			replacements.put("{region-displayname}", region.getDisplayName());
			replacements.put("{region-owner}", region.getOwner().getName());
			replacements.put("{region-bank}", Formatter.getBalance(region.getBank()));
			replacements.put("{region-createdat}", Formatter.getDate(region.getCreatedAt()));
			replacements.put("{region-rating}", Formatter.getRating(RegionsManager.getAverageRating(region)));
			replacements.put("{region-members}", String.valueOf(region.getMembers().size()));
			replacements.put("{region-chunks}", String.valueOf(region.getChunks().size()));

			items.add(MenuUtils.getButton(55, replacements));
		}

		return items;
	}

	public enum SortMode {
		BANK(18, 56, RegionsManager.RegionSorting.BANK),
		CHUNKS(19, 57, RegionsManager.RegionSorting.CHUNKS_COUNT),
		MEMBERS(20, 58, RegionsManager.RegionSorting.MEMBERS_COUNT),
		OLDEST(21, 59, RegionsManager.RegionSorting.CREATION_DATE),
		RATING(22, 60, RegionsManager.RegionSorting.RATING);

		final int titleId;
		final int buttonId;
		final RegionsManager.RegionSorting sorting;

		SortMode(int titleId, int buttonId, RegionsManager.RegionSorting sorting) {
			this.titleId = titleId;
			this.buttonId = buttonId;
			this.sorting = sorting;
		}

		public SortMode next() {
			SortMode[] values = values();
			return values[(this.ordinal() + 1) % values.length];
		}

		public SortMode previous() {
			SortMode[] values = values();
			return values[(this.ordinal() - 1 + values.length) % values.length];
		}
	}
}