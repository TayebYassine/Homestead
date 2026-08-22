package me.tayebyassine.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionLog;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.menus.MenuUtility;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class RegionLogs {
	private List<RegionLog> logs;

	public RegionLogs(Player player, Region region) {
		this.logs = LogManager.getLogs(region);

		PaginationMenu.builder(13, 9 * 5)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(getItems(player, region))
				.fillEmptySlots()
				.goBack((_player, event) -> new RegionMenu(player, region))
				.onClick((_player, context) -> handleLogClick(player, region, context))
				.actionButton(0, MenuUtility.getButton(46), handleMarkAllRead(player, region))
				.actionButton(2, MenuUtility.getButton(41), handleDeleteAll(player, region))
				.build()
				.open(player);
	}

	private static BiConsumer<Player, InventoryClickEvent> handleMarkAllRead(Player player, Region region) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (LogManager.getLogs(region).isEmpty()) {
				Messages.send(player, "commands.logs.1");
				return;
			}

			LogManager.markAllAsRead(region);



			Homestead.getInstance().runSyncTask(() -> new RegionLogs(player, region));
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleDeleteAll(Player player, Region region) {
		return (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
				Messages.send(player, "common.no_permission");
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}

			if (LogManager.getLogs(region).isEmpty()) {
				Messages.send(player, "commands.logs.1");
				return;
			}

			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			LogManager.deleteLogsOfRegion(region);

			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

			Homestead.getInstance().runSyncTask(() -> new RegionLogs(player, region));
		};
	}

	private void handleLogClick(Player player, Region region, PaginationMenu.ClickContext context) {
		if (context.getIndex() >= logs.size()) return;

		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player, ControlFlag.MANAGE_LOGS.getBitmask())) {
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		RegionLog log = logs.get(context.getIndex());

		if (context.getEvent().isLeftClick()) {
			log.setRead(true);
			refreshLogs(player, region, context);
		} else if (context.getEvent().isRightClick()) {
			handleDeleteLog(player, region, log, context);
		}
	}

	private void handleDeleteLog(Player player, Region region, RegionLog log, PaginationMenu.ClickContext context) {
		if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, "common.no_permission");
			return;
		}

		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		LogManager.deleteLog(log);
		refreshLogs(player, region, context);
	}

	private void refreshLogs(Player player, Region region, PaginationMenu.ClickContext context) {
		logs = LogManager.getLogs(region);
		context.getInstance().setItems(getItems(player, region));
	}

	private List<ItemStack> getItems(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < logs.size(); i++) {
			RegionLog log = logs.get(i);

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
		message = ColorTranslator.preserve(message);

		int wrapLength = 40;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < message.length(); i++) {
			if (i > 0 && i % wrapLength == 0) sb.append("\n");
			sb.append(message.charAt(i));
		}

		return sb.toString();
	}
}
