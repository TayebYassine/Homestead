package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.DelayedTeleport;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionsMenu {
	private static final Set<UUID> ADMIN_SHOW_ALL = ConcurrentHashMap.newKeySet();

	private final List<Region> REGIONS_ADMIN;
	private List<Region> regions = new ArrayList<>();

	public RegionsMenu(Player player) {
		this.REGIONS_ADMIN = RegionManager.getAll();
		this.regions = computeRegionList(player);

		PaginationMenu gui = PaginationMenu.builder(0, 9 * 4)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(getItems(player))
				.fillEmptySlots()
				.goBack((_player, event) -> _player.closeInventory())
				.onClick((_player, context) -> handleRegionClick(_player, player, context))
				.build();

		gui.open(player);
	}

	private static boolean isShowAllEnabled(Player p) {
		return PlayerUtility.isOperator(p) && ADMIN_SHOW_ALL.contains(p.getUniqueId());
	}

	private static void toggleShowAll(Player p) {
		if (!PlayerUtility.isOperator(p)) return;
		if (!ADMIN_SHOW_ALL.add(p.getUniqueId())) ADMIN_SHOW_ALL.remove(p.getUniqueId());
		PlayerSound.play(p, PlayerSound.PredefinedSound.CLICK);
	}

	private void handleRegionClick(Player clicker, Player menuPlayer, PaginationMenu.ClickContext context) {
		boolean hasToggle = PlayerUtility.isOperator(clicker);
		int index = context.getIndex();

		if (hasToggle && index == 0) {
			if (context.getEvent().isLeftClick()) {
				toggleShowAll(clicker);
				new RegionsMenu(clicker);
			}
			return;
		}

		if (hasToggle) index--;

		if (index < 0 || index >= regions.size()) return;

		Region region = regions.get(index);

		if (context.getEvent().isShiftClick() && context.getEvent().isRightClick()) {
			handleInfo(clicker, menuPlayer, region);
		} else if (context.getEvent().isRightClick()) {
			handleTeleport(clicker, menuPlayer, region, context);
		} else if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
			handleSelectTarget(clicker, menuPlayer, region, context);
		} else if (context.getEvent().isLeftClick()) {
			handleOpenMenu(clicker, menuPlayer, region);
		}
	}

	private void handleInfo(Player clicker, Player menuPlayer, Region region) {
		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			menuPlayer.closeInventory();
			return;
		}
		new RegionInfoMenu(clicker, region, () -> new RegionsMenu(clicker));
	}

	private void handleTeleport(Player clicker, Player menuPlayer, Region region, PaginationMenu.ClickContext context) {
		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			menuPlayer.closeInventory();
			return;
		}

		if (region.getLocation() == null) {
			Messages.send(clicker, 71, new Placeholder().add("{region}", region.getName()));
			return;
		}

		if (!menuPlayer.hasPermission("homestead.region.teleport")) {
			Messages.send(menuPlayer, 212);
			return;
		}

		boolean allowed = PlayerUtility.isOperator(clicker)
				|| region.isOwner(menuPlayer)
				|| (PlayerUtility.hasPermissionFlag(region.getUniqueId(), clicker, PlayerFlags.TELEPORT_SPAWN, true)
				&& PlayerUtility.hasPermissionFlag(region.getUniqueId(), clicker, PlayerFlags.PASSTHROUGH, true))
				&& clicker.hasPermission("homestead.region.teleport");

		if (!allowed) {
			Messages.send(clicker, 45, new Placeholder().add("{region}", region.getName()));
			return;
		}

		menuPlayer.closeInventory();
		new DelayedTeleport(clicker, region.getLocation().toBukkit());
	}

	private void handleSelectTarget(Player clicker, Player menuPlayer, Region region, PaginationMenu.ClickContext context) {
		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			menuPlayer.closeInventory();
			return;
		}

		Region current = TargetRegionSession.getRegion(clicker);
		if (current != null && current.getUniqueId() == region.getUniqueId()) return;

		TargetRegionSession.newSession(clicker, region);
		PlayerSound.play(menuPlayer, PlayerSound.PredefinedSound.CLICK);
		Messages.send(clicker, 12, new Placeholder().add("{region}", region.getName()));

		regions = computeRegionList(clicker);
		context.getInstance().setItems(getItems(clicker));
	}

	private void handleOpenMenu(Player clicker, Player menuPlayer, Region region) {
		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			menuPlayer.closeInventory();
			return;
		}
		new RegionMenu(clicker, region);
	}

	private List<Region> computeRegionList(Player player) {
		if (isShowAllEnabled(player)) return REGIONS_ADMIN;

		List<Region> list = new ArrayList<>();
		list.addAll(RegionManager.getRegionsOwnedByPlayer(player));
		list.addAll(RegionManager.getRegionsHasPlayerAsMember(player));
		return ListUtils.removeDuplications(list);
	}

	private List<ItemStack> getItems(Player player) {
		List<ItemStack> items = new ArrayList<>();

		if (PlayerUtility.isOperator(player)) {
			items.add(isShowAllEnabled(player) ? MenuUtility.getButton(62) : MenuUtility.getButton(63));
		}

		Region targetRegion = TargetRegionSession.getRegion(player);

		for (Region region : regions) {
			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{region-displayname}", region.getDisplayName())
					.add("{region-owner}", region.getOwnerName())
					.add("{region-bank}", Formatter.getBalance(region.getBank()))
					.add("{region-createdat}", Formatter.getDate(region.getCreatedAt()));

			boolean isTarget = targetRegion != null && targetRegion.getUniqueId() == region.getUniqueId();
			items.add(MenuUtility.getButton(isTarget ? 5 : 4, placeholder));
		}

		return items;
	}
}