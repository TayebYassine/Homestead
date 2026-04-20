package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableLog;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public final class RegionLogs {
	private List<SerializableLog> logs;

	public RegionLogs(Player player, Region region) {
		logs = region.getLogs(true);

		PaginationMenu gui = new PaginationMenu(
				MenuUtility.getTitle(13), 9 * 5,
				MenuUtility.getNextPageButton(),
				MenuUtility.getPreviousPageButton(),
				getItems(player, region),
				(_player, event) -> new RegionMenu(player, region),
				(_player, context) -> {
					if (context.getIndex() >= logs.size()) return;

					if (RegionManager.findRegion(region.getUniqueId()) == null) {
						player.closeInventory();
						return;
					}

					if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
							RegionControlFlags.MANAGE_LOGS)) {
						return;
					}

					SerializableLog log = logs.get(context.getIndex());

					if (context.getEvent().isLeftClick()) {
						region.setLogAsRead(log.getId());
						logs = region.getLogs(true);
						context.getInstance().setItems(getItems(player, region));

					} else if (context.getEvent().isRightClick()) {
						if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
							Messages.send(player, 159);
							return;
						}

						region.removeLog(log.getId());
						logs = region.getLogs(true);
						context.getInstance().setItems(getItems(player, region));
					}
				});

		gui.addActionButton(0, MenuUtility.getButton(46), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (region.getLogs().isEmpty()) {
				Messages.send(player, 91);
				return;
			}

			region.getLogs().forEach(log -> region.setLogAsRead(log.getId()));
			Messages.send(player, 92);
			Homestead.getInstance().runSyncTask(() -> new RegionLogs(player, region));
		});

		gui.addActionButton(2, MenuUtility.getButton(41), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
				Messages.send(player, 159);
				return;
			}
			if (region.getLogs().isEmpty()) {
				Messages.send(player, 83);
				return;
			}

			region.setLogs(new ArrayList<>());
			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
			Messages.send(player, 93);
			Homestead.getInstance().runSyncTask(() -> new RegionLogs(player, region));
		});

		gui.open(player, MenuUtility.getEmptySlot());
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < logs.size(); i++) {
			SerializableLog log = logs.get(i);

			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{index}", i + 1)
					.add("{log-sentat}", Formatter.getDate(log.getSentAt()))
					.add("{log-author}", log.getAuthor())
					.add("{log-message}", wrapMessage(log.getMessage()));

			items.add(MenuUtility.getButton(log.isRead() ? 40 : 39, placeholder));
		}

		return items;
	}

	private String wrapMessage(String message) {
		int wrapLength = 40;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < message.length(); i++) {
			if (i > 0 && i % wrapLength == 0) sb.append("\n");
			sb.append(message.charAt(i));
		}

		return sb.toString();
	}
}