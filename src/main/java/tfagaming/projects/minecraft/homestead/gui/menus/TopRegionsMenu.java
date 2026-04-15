package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;

import java.util.ArrayList;
import java.util.List;

public final class TopRegionsMenu {
	private final boolean isPublicRegionsOnly;
	private List<Region> regions;

	public TopRegionsMenu(Player player, boolean isPublic) {
		this(player, isPublic, SortMode.BANK);
	}

	public TopRegionsMenu(Player player, boolean isPublic, SortMode sortMode) {
		this.isPublicRegionsOnly = isPublic;

		regions = new ArrayList<>(RegionManager.sortRegions(sortMode.sorting));
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

					if (RegionManager.findRegion(region.getUniqueId()) == null) {
						player.closeInventory();
						return;
					}

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

		gui.addActionButton(2, MenuUtils.getButton(81, new Placeholder()
				.add("{is-public-regions}", Formatter.getToggle(this.isPublicRegionsOnly))
		), (_player, event) -> {
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

			Placeholder placeholder = new Placeholder()
					.add("{rank}", String.valueOf(i + 1))
					.add("{region}", region.getName())
					.add("{region-displayname}", region.getDisplayName())
					.add("{region-owner}", region.getOwner().getName())
					.add("{region-bank}", Formatter.getBalance(region.getBank()))
					.add("{region-createdat}", Formatter.getDate(region.getCreatedAt()))
					.add("{region-rating}", Formatter.getRating(RegionManager.getAverageRating(region)))
					.add("{region-members}", String.valueOf(region.getMembers().size()))
					.add("{region-chunks}", String.valueOf(region.getChunks().size()));

			items.add(MenuUtils.getButton(55, placeholder));
		}

		return items;
	}

	public enum SortMode {
		BANK(18, 56, RegionManager.RegionSorting.BANK),
		CHUNKS(19, 57, RegionManager.RegionSorting.CHUNKS_COUNT),
		MEMBERS(20, 58, RegionManager.RegionSorting.MEMBERS_COUNT),
		OLDEST(21, 59, RegionManager.RegionSorting.CREATION_DATE),
		RATING(22, 60, RegionManager.RegionSorting.RATING);

		final int titleId;
		final int buttonId;
		final RegionManager.RegionSorting sorting;

		SortMode(int titleId, int buttonId, RegionManager.RegionSorting sorting) {
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