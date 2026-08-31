package me.tayebyassine.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.PlayerLeftRegionEvent;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.Menu;
import me.tayebyassine.homestead.managers.*;
import me.tayebyassine.homestead.managers.RegionManager.RegionSorting;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.economy.UpkeepUtility;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import me.tayebyassine.homestead.util.minecraft.menus.MenuUtility;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import me.tayebyassine.homestead.weatherandtime.RegionTime;
import me.tayebyassine.homestead.weatherandtime.RegionWeather;

import java.util.function.BiConsumer;

public final class RegionMenu {
	public RegionMenu(Player player, Region region) {
		boolean isEconomyEnabled = Homestead.VAULT.isEconomyReady();
		boolean isUpkeepEnabled = isEconomyEnabled && Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("upkeep.enabled");
		boolean isRentEnabled = isEconomyEnabled && Resources.<RegionsFile>get(ResourceType.Regions).isRentingEnabled();
		boolean isSubAreasEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("sub-areas.enabled");

		SeRent rent = region.getRent();

		Placeholder placeholder = new Placeholder()
				.add("{region}", region.getName())
				.add("{region-owner}", region.getOwnerName())
				.add("{region-bank}", Formatter.getBalance(region.getBank()))
				.add("{region-createdat}", Formatter.getDate(region.getCreatedAt()))
				.add("{region-chunks}", ChunkManager.getChunksOfRegion(region).size())
				.add("{region-chunks-max}", Limits.getRegionLimit(region, Limits.LimitType.CHUNKS_PER_REGION))
				.add("{region-members}", MemberManager.getMembersOfRegion(region).size())
				.add("{region-members-max}", Limits.getRegionLimit(region, Limits.LimitType.MEMBERS_PER_REGION))
				.add("{upkeep-enabled}", Formatter.getToggle(isUpkeepEnabled))
				.add("{upkeep-date}", isUpkeepEnabled ? Formatter.getRemainingTime(region.getUpkeepAt()) : Formatter.getNever())
				.add("{upkeep-amount}", Formatter.getBalance(UpkeepUtility.getAmountToPay(region)))
				.add("{region-global-rank}", RegionManager.getGlobalRank(region.getUniqueId()))
				.add("{region-rank-bank}", RegionManager.getRank(RegionSorting.BANK, region.getUniqueId()))
				.add("{region-rank-chunks}", RegionManager.getRank(RegionSorting.CHUNKS_COUNT, region.getUniqueId()))
				.add("{region-rank-members}", RegionManager.getRank(RegionSorting.MEMBERS_COUNT, region.getUniqueId()))
				.add("{region-rank-rating}", RegionManager.getRank(RegionSorting.RATING, region.getUniqueId()))
				.add("{region-logs}", LogManager.getLogs(region).size())
				.add("{region-logs-unread}", LogManager.getLogs(region).stream().filter(log -> !log.isRead()).count())
				.add("{region-weather}", RegionWeather.from(region.getWeather()))
				.add("{region-time}", RegionTime.from(region.getTime()))
				.add("{subareas-enabled}", Formatter.getToggle(isSubAreasEnabled))
				.add("{region-subareas}", SubAreaManager.getSubAreasOfRegion(region.getUniqueId()).size())
				.add("{region-subareas-max}", Limits.getRegionLimit(region, Limits.LimitType.SUBAREAS_PER_REGION))
				.add("{rent-enabled}", Formatter.getToggle(isRentEnabled));

		Menu.Builder<?> builder = Menu.builder(MenuUtility.getTitle(1).replace("{region}", region.getName()), 9 * 4)
				.button(10, MenuUtility.getButton(6, placeholder), handlePlayersManagement(player, region))
				.button(11, MenuUtility.getButton(7, placeholder), handleClaimedChunks(player, region))
				.button(12, MenuUtility.getButton(8, placeholder), handleFlags(player, region))
				.button(13, MenuUtility.getButton(9, placeholder), handleMiscSettings(player, region))
				.button(14, MenuUtility.getButton(10, placeholder), handleSubAreas(player, region))
				.button(20, MenuUtility.getButton(79, placeholder), handleRewards(player, region))
				.item(21, MenuUtility.getButton(11, placeholder))
				.button(22, MenuUtility.getButton(12, placeholder), handleOpenRentMenu(player, region))
				.button(23, MenuUtility.getButton(80, placeholder), handleLevels(player, region))
				.item(24, MenuUtility.getButton(15, placeholder))
				.button(15, MenuUtility.getButton(13, placeholder), handleLogs(player, region))
				.button(16, MenuUtility.getButton(16, placeholder), handleWeatherTime(player, region))
				.button(27, MenuUtility.getBackButton(), handleBack(player, region));

		if (MemberManager.isMemberOfRegion(region, player)) {
			builder.button(35, MenuUtility.getButton(14, placeholder), handleLeaveRegion(player, region));
		}

		builder.fillEmptySlots()
				.build()
				.open(player);
	}

	private static BiConsumer<Player, InventoryClickEvent> handlePlayersManagement(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegionExists(player, region) && event.isLeftClick()) {
				new RegionPlayersManagement(player, region);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleClaimedChunks(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegionExists(player, region) && event.isLeftClick()) {
				new RegionClaimedChunks(player, region);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleFlags(Player player, Region region) {
		return (_player, event) -> {
			if (!checkRegionExists(player, region)) return;

			if (event.isLeftClick()) {
				new GlobalPlayerFlags(player, region);
			} else if (event.isRightClick()) {
				new RegionWorldFlags(player, region);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleMiscSettings(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegionExists(player, region) && event.isLeftClick()) {
				new MiscellaneousSettings(player, region);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleSubAreas(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegionExists(player, region) && event.isLeftClick()) {
				new SubAreasMenu(player, region);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleRewards(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegionExists(player, region) && event.isLeftClick()) {
				new Rewards(player, region, () -> new RegionMenu(player, region));
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleOpenRentMenu(Player player, Region region) {
		return (_player, event) -> {
			if (!checkRegionExists(player, region) || !event.isLeftClick()) return;

			if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
				Messages.send(player, "common.no_permission");
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}

			new RentConfigMenu(player, region, null);
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleLevels(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegionExists(player, region) && event.isLeftClick()) {
				new RegionLevels(player, region, () -> new RegionMenu(player, region));
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleLogs(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegionExists(player, region) && event.isLeftClick()) {
				new RegionLogs(player, region);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleWeatherTime(Player player, Region region) {
		return (_player, event) -> {
			if (!checkRegionExists(player, region)) return;

			if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.SET_WEATHER_AND_TIME, true)) {
				PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
				return;
			}

			if (event.isLeftClick()) {
				if (!player.hasPermission("homestead.actions.regions.update.weather")) {
					Messages.send(player, "common.no_permission");
					PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
					return;
				}

				int newWeather = RegionWeather.next(region.getWeather());

				region.setWeather(newWeather);

				LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_WEATHER, RegionWeather.from(newWeather));
			} else if (event.isRightClick()) {
				if (!player.hasPermission("homestead.actions.regions.update.time")) {
					Messages.send(player, "common.no_permission");
					PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
					return;
				}

				int newTime = RegionTime.next(region.getTime());

				region.setTime(newTime);

				LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_TIME, RegionTime.from(newTime));
			}

			PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
			new RegionMenu(player, region);
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleBack(Player player, Region region) {
		return (_player, event) -> {
			if (checkRegionExists(player, region) && event.isLeftClick()) {
				new RegionsMenu(player);
			}
		};
	}

	private static BiConsumer<Player, InventoryClickEvent> handleLeaveRegion(Player player, Region region) {
		return (_player, event) -> {
			if (!checkRegionExists(player, region) || !event.isLeftClick()) return;

			MemberManager.removeMemberFromRegion(player, region);

			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

			LogManager.addLog(region, player, LogManager.PredefinedLog.UNTRUST_PLAYER, player.getName());

			TargetRegionSession.randomizeRegion(player);

			Homestead.callEvent(new PlayerLeftRegionEvent(region, player));

			new RegionsMenu(player);
		};
	}

	private static boolean checkRegionExists(Player player, Region region) {
		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return false;
		}
		return true;
	}
}
