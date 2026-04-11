package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionsMenu {
	private static final Set<UUID> ADMIN_SHOW_ALL = ConcurrentHashMap.newKeySet();

	private List<Region> regions = new ArrayList<>();

	public RegionsMenu(Player player) {
		this.regions = computeRegionList(player);

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(0), 9 * 4,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				getItems(player),
				(_player, event) -> _player.closeInventory(),
				(_player, context) -> {
					boolean hasToggle = PlayerUtils.isOperator(_player);
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
						new RegionInfoMenu(_player, region, () -> new RegionsMenu(_player));
						return;
					}

					if (context.getEvent().isRightClick()) {
						if (region.getLocation() == null) {
							Messages.send(_player, 71, new Placeholder().add("{region}", region.getName()));
							return;
						}

						if(!player.hasPermission("homestead.region.teleport")){
							Messages.send(player, 212);
							return;
						}

						boolean allowed = PlayerUtils.isOperator(_player)
								|| region.isOwner(player)
								|| (PlayerUtils.hasPermissionFlag(region.getUniqueId(), _player, PlayerFlags.TELEPORT_SPAWN, true)
								&& PlayerUtils.hasPermissionFlag(region.getUniqueId(), _player, PlayerFlags.PASSTHROUGH, true))
								&& player.hasPermission("homestead.region.teleport");

						if (!allowed) {
							Messages.send(_player, 45, new Placeholder().add("{region}", region.getName()));
							return;
						}

						new DelayedTeleport(_player, region.getLocation().bukkit());
						return;
					}

					if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
						Region current = TargetRegionSession.getRegion(_player);
						if (current != null && current.getUniqueId().equals(region.getUniqueId())) return;

						TargetRegionSession.newSession(_player, region);
						PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
						Messages.send(_player, 12, new Placeholder().add("{region}", region.getName()));

						regions = computeRegionList(_player);
						context.getInstance().setItems(getItems(_player));
						return;
					}

					if (context.getEvent().isLeftClick()) {
						new RegionMenu(_player, region);
					}
				});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	private static boolean isShowAllEnabled(Player p) {
		return PlayerUtils.isOperator(p) && ADMIN_SHOW_ALL.contains(p.getUniqueId());
	}

	private static void toggleShowAll(Player p) {
		if (!PlayerUtils.isOperator(p)) return;

		if (!ADMIN_SHOW_ALL.add(p.getUniqueId())) ADMIN_SHOW_ALL.remove(p.getUniqueId());

		PlayerSound.play(p, PlayerSound.PredefinedSound.CLICK);
	}

	private List<Region> computeRegionList(Player player) {
		if (isShowAllEnabled(player)) return new ArrayList<>(RegionManager.getAll());

		List<Region> list = new ArrayList<>();
		list.addAll(RegionManager.getRegionsOwnedByPlayer(player));
		list.addAll(RegionManager.getRegionsHasPlayerAsMember(player));
		return ListUtils.removeDuplications(list);
	}

	private List<ItemStack> getItems(Player player) {
		List<ItemStack> items = new ArrayList<>();

		if (PlayerUtils.isOperator(player)) {
			items.add(isShowAllEnabled(player) ? MenuUtils.getButton(62) : MenuUtils.getButton(63));
		}

		Region targetRegion = TargetRegionSession.getRegion(player);

		for (Region region : regions) {
			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{region-displayname}", region.getDisplayName())
					.add("{region-owner}", region.getOwner().getName())
					.add("{region-bank}", Formatter.getBalance(region.getBank()))
					.add("{region-createdat}", Formatter.getDate(region.getCreatedAt()));

			boolean isTarget = targetRegion != null && targetRegion.getUniqueId().equals(region.getUniqueId());
			items.add(MenuUtils.getButton(isTarget ? 5 : 4, placeholder));
		}

		return items;
	}
}