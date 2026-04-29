package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionUntrustPlayerEvent;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.managers.RegionManager.RegionSorting;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;

import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.economy.UpkeepUtility;
import tfagaming.projects.minecraft.homestead.weatherandtime.TimeType;
import tfagaming.projects.minecraft.homestead.weatherandtime.WeatherType;

import java.util.function.BiConsumer;

public final class RegionMenu {
	public RegionMenu(Player player, Region region) {
		boolean isEconomyEnabled = Homestead.vault.isEconomyReady();
		boolean isUpkeepEnabled = isEconomyEnabled && Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("upkeep.enabled");
		boolean isRentEnabled = isEconomyEnabled && Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("renting.enabled");
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
				.add("{region-weather}", WeatherType.from(region.getWeather()))
				.add("{region-time}", TimeType.from(region.getTime()))
				.add("{subareas-enabled}", Formatter.getToggle(isSubAreasEnabled))
				.add("{region-subareas}", SubAreaManager.getSubAreasOfRegion(region.getUniqueId()).size())
				.add("{region-subareas-max}", Limits.getRegionLimit(region, Limits.LimitType.SUBAREAS_PER_REGION))
				.add("{rent-enabled}", Formatter.getToggle(isRentEnabled))
				.add("{rent-renter}", rent != null ? rent.getRenterName() : Formatter.getNone())
				.add("{rent-price}", rent != null ? Formatter.getBalance(rent.getPrice()) : Formatter.getNone())
				.add("{rent-until}", rent != null ? Formatter.getRemainingTime(rent.getUntilAt()) : Formatter.getNever());

		Menu.Builder<?> builder = Menu.builder(MenuUtility.getTitle(1).replace("{region}", region.getName()), 9 * 4)
				.button(10, MenuUtility.getButton(6, placeholder), handlePlayersManagement(player, region))
				.button(11, MenuUtility.getButton(7, placeholder), handleClaimedChunks(player, region))
				.button(12, MenuUtility.getButton(8, placeholder), handleFlags(player, region))
				.button(13, MenuUtility.getButton(9, placeholder), handleMiscSettings(player, region))
				.button(14, MenuUtility.getButton(10, placeholder), handleSubAreas(player, region))
				.button(20, MenuUtility.getButton(79, placeholder), handleRewards(player, region))
				.item(21, MenuUtility.getButton(11, placeholder))
				.button(22, MenuUtility.getButton(12, placeholder), handleEndRent(player, region))
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

	private static BiConsumer<Player, InventoryClickEvent> handleEndRent(Player player, Region region) {
		return (_player, event) -> {
			if (!checkRegionExists(player, region) || !event.isLeftClick()) return;

			if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
				Messages.send(player, 159);
				return;
			}

			if (region.getRent() == null) {
				Messages.send(player, 128);
			} else {
				region.setRent(null);
				Messages.send(player, 127);
				new RegionMenu(player, region);
			}
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
			if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
					RegionControlFlags.SET_WEATHER_AND_TIME)) {
				return;
			}

			if (event.isLeftClick()) {
				if (!player.hasPermission("homestead.region.weather")) {
					Messages.send(player, 210);
					return;
				}

				region.setWeather(WeatherType.next(region.getWeather()));

				LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_WEATHER);
			} else if (event.isRightClick()) {
				if (!player.hasPermission("homestead.region.time")) {
					Messages.send(player, 211);
					return;
				}

				region.setTime(TimeType.next(region.getTime()));

				LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_TIME);
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
			TargetRegionSession.randomizeRegion(player);

			PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);

			LogManager.addLog(region, player, LogManager.PredefinedLog.UNTRUST_PLAYER, player.getName());

			RegionUntrustPlayerEvent _event = new RegionUntrustPlayerEvent(region, player, player,
					RegionUntrustPlayerEvent.UntrustReason.LEFT);
			Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

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