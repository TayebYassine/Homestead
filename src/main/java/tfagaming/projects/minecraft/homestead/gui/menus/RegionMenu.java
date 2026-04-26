package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

public final class RegionMenu {
	public RegionMenu(Player player, Region region) {
		Menu gui = new Menu(MenuUtility.getTitle(1).replace("{region}", region.getName()), 9 * 4);

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
				// Rent placeholders
				.add("{rent-enabled}", Formatter.getToggle(isRentEnabled))
				.add("{rent-renter}", rent != null ? rent.getRenterName() : Formatter.getNone())
				.add("{rent-price}", rent != null ? Formatter.getBalance(rent.getPrice()) : Formatter.getNone())
				.add("{rent-until}", rent != null ? Formatter.getRemainingTime(rent.getUntilAt()) : Formatter.getNever());

		gui.addItem(10, MenuUtility.getButton(6, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new RegionPlayersManagement(player, region);
		});

		gui.addItem(11, MenuUtility.getButton(7, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new RegionClaimedChunks(player, region);
		});

		gui.addItem(12, MenuUtility.getButton(8, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (event.isLeftClick()) {
				new GlobalPlayerFlags(player, region);
			} else if (event.isRightClick()) {
				new RegionWorldFlags(player, region);
			}
		});

		gui.addItem(13, MenuUtility.getButton(9, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new MiscellaneousSettings(player, region);
		});

		gui.addItem(14, MenuUtility.getButton(10, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new SubAreasMenu(player, region);
		});

		gui.addItem(20, MenuUtility.getButton(79, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new Rewards(player, region, () -> new RegionMenu(player, region));
		});

		gui.addItem(21, MenuUtility.getButton(11, placeholder), null);

		gui.addItem(22, MenuUtility.getButton(12, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;

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
		});

		gui.addItem(23, MenuUtility.getButton(80, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new RegionLevels(player, region, () -> new RegionMenu(player, region));
		});

		gui.addItem(24, MenuUtility.getButton(15, placeholder), null);

		gui.addItem(15, MenuUtility.getButton(13, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new RegionLogs(player, region);
		});

		gui.addItem(16, MenuUtility.getButton(16, placeholder), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

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
			} else if (event.isRightClick()) {
				if (!player.hasPermission("homestead.region.time")) {
					Messages.send(player, 211);
					return;
				}
				region.setTime(TimeType.next(region.getTime()));
			}

			PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
			new RegionMenu(player, region);
		});

		gui.addItem(27, MenuUtility.getBackButton(), (_player, event) -> {
			if (RegionManager.findRegion(region.getUniqueId()) == null) {
				player.closeInventory();
				return;
			}

			if (!event.isLeftClick()) return;
			new RegionsMenu(player);
		});

		if (MemberManager.isMemberOfRegion(region, player)) {
			gui.addItem(35, MenuUtility.getButton(14, placeholder), (_player, event) -> {
				if (RegionManager.findRegion(region.getUniqueId()) == null) {
					player.closeInventory();
					return;
				}

				if (!event.isLeftClick()) return;

				MemberManager.removeMemberFromRegion(player, region);

				TargetRegionSession.randomizeRegion(player);

				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				/*RegionManager.addNewLog(region.getUniqueId(), 4, new Placeholder()
						.add("{playername}", player.getName()));*/

				RegionUntrustPlayerEvent _event = new RegionUntrustPlayerEvent(region, player, player, RegionUntrustPlayerEvent.UntrustReason.LEFT);
				Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

				new RegionsMenu(player);
			});
		}

		gui.open(player, MenuUtility.getEmptySlot());
	}
}