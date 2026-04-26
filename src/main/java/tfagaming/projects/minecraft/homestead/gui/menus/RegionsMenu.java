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
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.DelayedTeleport;

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

		PaginationMenu gui = new PaginationMenu(
				MenuUtility.getTitle(0), 9 * 4,
				MenuUtility.getNextPageButton(),
				MenuUtility.getPreviousPageButton(),
				getItems(player),
				(_player, event) -> _player.closeInventory(),
				(_player, context) -> {
					boolean hasToggle = PlayerUtility.isOperator(_player);
					int index = context.getIndex();

					if (hasToggle && index == 0) {
						if (context.getEvent().isLeftClick()) {
							toggleShowAll(_player);
							new RegionsMenu(_player);
						}
						return;
					}

					if (hasToggle) index--;

					if (index < 0 || index >= regions.size()) return;

					Region region = regions.get(index);

					if (context.getEvent().isShiftClick() && context.getEvent().isRightClick()) {
						if (RegionManager.findRegion(region.getUniqueId()) == null) {
							player.closeInventory();
							return;
						}

						new RegionInfoMenu(_player, region, () -> new RegionsMenu(_player));
						return;
					}

					if (context.getEvent().isRightClick()) {
						if (RegionManager.findRegion(region.getUniqueId()) == null) {
							player.closeInventory();
							return;
						}

						if (region.getLocation() == null) {
							Messages.send(_player, 71, new Placeholder().add("{region}", region.getName()));
							return;
						}

						if (!player.hasPermission("homestead.region.teleport")) {
							Messages.send(player, 212);
							return;
						}

						boolean allowed = PlayerUtility.isOperator(_player)
								|| region.isOwner(player)
								|| (PlayerUtility.hasPermissionFlag(region.getUniqueId(), _player, PlayerFlags.TELEPORT_SPAWN, true)
								&& PlayerUtility.hasPermissionFlag(region.getUniqueId(), _player, PlayerFlags.PASSTHROUGH, true))
								&& player.hasPermission("homestead.region.teleport");

						if (!allowed) {
							Messages.send(_player, 45, new Placeholder().add("{region}", region.getName()));
							return;
						}

						player.closeInventory();

						new DelayedTeleport(_player, region.getLocation().toBukkit());

						return;
					}

					if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
						if (RegionManager.findRegion(region.getUniqueId()) == null) {
							player.closeInventory();
							return;
						}

						Region current = TargetRegionSession.getRegion(_player);
						if (current != null && current.getUniqueId() == region.getUniqueId()) return;

						TargetRegionSession.newSession(_player, region);
						PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
						Messages.send(_player, 12, new Placeholder().add("{region}", region.getName()));

						regions = computeRegionList(_player);
						context.getInstance().setItems(getItems(_player));
						return;
					}

					if (context.getEvent().isLeftClick()) {
						if (RegionManager.findRegion(region.getUniqueId()) == null) {
							player.closeInventory();
							return;
						}

						new RegionMenu(_player, region);
					}
				});

		gui.open(player, MenuUtility.getEmptySlot());
	}

	private static boolean isShowAllEnabled(Player p) {
		return PlayerUtility.isOperator(p) && ADMIN_SHOW_ALL.contains(p.getUniqueId());
	}

	private static void toggleShowAll(Player p) {
		if (!PlayerUtility.isOperator(p)) return;

		if (!ADMIN_SHOW_ALL.add(p.getUniqueId())) ADMIN_SHOW_ALL.remove(p.getUniqueId());

		PlayerSound.play(p, PlayerSound.PredefinedSound.CLICK);
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