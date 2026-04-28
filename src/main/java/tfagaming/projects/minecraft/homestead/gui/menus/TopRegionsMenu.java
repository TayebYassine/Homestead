package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RateManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class TopRegionsMenu {
	private final boolean isPublicRegionsOnly;
	private List<Region> regions;

	public TopRegionsMenu(Player player, boolean isPublic) {
		this(player, isPublic, SortMode.BANK);
	}

	public TopRegionsMenu(Player player, boolean isPublic, SortMode sortMode) {
		this.isPublicRegionsOnly = isPublic;

		this.regions = new ArrayList<>(RegionManager.sortRegions(sortMode.sorting));
		this.regions = ListUtils.removeDuplications(regions);

		PaginationMenu gui = PaginationMenu.builder(MenuUtility.getTitle(sortMode.titleId), 9 * 5)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(getItems(player))
				.fillEmptySlots()
				.goBack((_player, event) -> _player.closeInventory())
				.onClick((_player, context) -> handleRegionClick(player, context, sortMode))
				.actionButton(0, MenuUtility.getButton(sortMode.buttonId), handleCycleSort(player, sortMode))
				.actionButton(2, MenuUtility.getButton(81, new Placeholder()
						.add("{is-public-regions}", Formatter.getToggle(this.isPublicRegionsOnly))), handleTogglePublic(player, sortMode))
				.build();

		gui.open(player);
	}

	private void handleRegionClick(Player player, PaginationMenu.ClickContext context, SortMode sortMode) {
		if (context.getIndex() >= regions.size()) return;

		Region region = regions.get(context.getIndex());

		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		if (context.getEvent().isLeftClick()) {
			new RegionInfoMenu(player, region, () -> new TopRegionsMenu(player, this.isPublicRegionsOnly, sortMode));
		}
	}

	private BiConsumer<Player, InventoryClickEvent> handleCycleSort(Player player, SortMode sortMode) {
		return (_player, event) -> {
			if (event.isLeftClick()) {
				new TopRegionsMenu(player, this.isPublicRegionsOnly, sortMode.next());
			} else if (event.isRightClick()) {
				new TopRegionsMenu(player, this.isPublicRegionsOnly, sortMode.previous());
			}
			PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
		};
	}

	private BiConsumer<Player, InventoryClickEvent> handleTogglePublic(Player player, SortMode sortMode) {
		return (_player, event) -> {
			if (event.isLeftClick()) {
				new TopRegionsMenu(player, !this.isPublicRegionsOnly, sortMode);
			}
		};
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
					.add("{region-owner}", region.getOwnerName())
					.add("{region-bank}", Formatter.getBalance(region.getBank()))
					.add("{region-createdat}", Formatter.getDate(region.getCreatedAt()))
					.add("{region-rating}", Formatter.getRating(RateManager.getAverageRating(region)))
					.add("{region-members}", String.valueOf(MemberManager.getMembersOfRegion(region).size()))
					.add("{region-chunks}", String.valueOf(ChunkManager.getChunksOfRegion(region).size()));

			items.add(MenuUtility.getButton(55, placeholder));
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