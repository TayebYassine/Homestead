package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager.RegionSorting;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.other.UpkeepUtils;
import tfagaming.projects.minecraft.homestead.weatherandtime.TimeType;
import tfagaming.projects.minecraft.homestead.weatherandtime.WeatherType;

public class RegionMenu {
	public RegionMenu(Player player, Region region) {
		Menu gui = new Menu(MenuUtils.getTitle(1).replace("{region}", region.getName()), 9 * 4);

		boolean isEconomyEnabled = Homestead.vault.isEconomyReady();
		boolean isUpkeepEnabled = isEconomyEnabled && Homestead.config.getBoolean("upkeep.enabled");
		boolean isRentEnabled = isEconomyEnabled && Homestead.config.getBoolean("renting.enabled");
		boolean isSubAreasEnabled = Homestead.config.getBoolean("sub-areas.enabled");

		SerializableRent rent = region.getRent();

		Placeholder placeholder = new Placeholder()
				.add("{region}", region.getName())
				.add("{region-owner}", region.getOwner().getName())
				.add("{region-bank}", Formatter.getBalance(region.getBank()))
				.add("{region-createdat}", Formatter.getDate(region.getCreatedAt()))
				.add("{region-chunks}", region.getChunks().size())
				.add("{region-members}", region.getMembers().size())
				.add("{upkeep-enabled}", Formatter.getToggle(isUpkeepEnabled))
				.add("{upkeep-date}", isUpkeepEnabled ? Formatter.getRemainingTime(region.getUpkeepAt()) : Formatter.getNever())
				.add("{upkeep-amount}", Formatter.getBalance(UpkeepUtils.getAmountToPay(region)))
				.add("{region-global-rank}", RegionManager.getGlobalRank(region.getUniqueId()))
				.add("{region-rank-bank}", RegionManager.getRank(RegionSorting.BANK, region.getUniqueId()))
				.add("{region-rank-chunks}", RegionManager.getRank(RegionSorting.CHUNKS_COUNT, region.getUniqueId()))
				.add("{region-rank-members}", RegionManager.getRank(RegionSorting.MEMBERS_COUNT, region.getUniqueId()))
				.add("{region-rank-rating}", RegionManager.getRank(RegionSorting.RATING, region.getUniqueId()))
				.add("{region-logs}", region.getLogs().size())
				.add("{region-logs-unread}", region.getLogs().stream().filter(log -> !log.isRead()).count())
				.add("{region-weather}", WeatherType.from(region.getWeather()))
				.add("{region-time}", TimeType.from(region.getTime()))
				.add("{subareas-enabled}", Formatter.getToggle(isSubAreasEnabled))
				.add("{region-subareas}", SubAreaManager.getSubAreasOfRegion(region.getUniqueId()).size())
				// Rent placeholders
				.add("{rent-enabled}", Formatter.getToggle(isRentEnabled))
				.add("{rent-renter}", rent != null ? rent.getPlayer().getName() : Formatter.getNone())
				.add("{rent-price}", rent != null ? Formatter.getBalance(rent.getPrice()) : Formatter.getNone())
				.add("{rent-until}", rent != null ? Formatter.getRemainingTime(rent.getUntilAt()) : Formatter.getNever());

		gui.addItem(10, MenuUtils.getButton(6, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new RegionPlayersManagement(player, region);
		});

		gui.addItem(11, MenuUtils.getButton(7, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new RegionClaimedChunks(player, region);
		});

		gui.addItem(12, MenuUtils.getButton(8, placeholder), (_player, event) -> {
			if (event.isLeftClick()) {
				if (!player.hasPermission("homestead.region.flags.global")) {
					Messages.send(player, 8);
					return;
				}
				new GlobalPlayerFlags(player, region);
			} else if (event.isRightClick()) {
				if (!player.hasPermission("homestead.region.flags.world")) {
					Messages.send(player, 8);
					return;
				}
				new RegionWorldFlags(player, region);
			}
		});

		gui.addItem(13, MenuUtils.getButton(9, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new MiscellaneousSettings(player, region);
		});

		gui.addItem(14, MenuUtils.getButton(10, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new SubAreasMenu(player, region);
		});

		gui.addItem(20, MenuUtils.getButton(79, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new Rewards(player, region, () -> new RegionMenu(player, region));
		});

		gui.addItem(21, MenuUtils.getButton(11, placeholder), null);

		gui.addItem(22, MenuUtils.getButton(12, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;

			if (!PlayerUtils.isOperator(player) && !region.isOwner(player)) {
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

		gui.addItem(23, MenuUtils.getButton(80, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new RegionLevels(player, region, () -> new RegionMenu(player, region));
		});

		gui.addItem(24, MenuUtils.getButton(15, placeholder), null);

		gui.addItem(15, MenuUtils.getButton(13, placeholder), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new RegionLogs(player, region);
		});

		gui.addItem(16, MenuUtils.getButton(16, placeholder), (_player, event) -> {
			if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
					RegionControlFlags.SET_WEATHER_AND_TIME)) {
				return;
			}

			if (event.isLeftClick()) {
				region.setWeather(WeatherType.next(region.getWeather()));
			} else if (event.isRightClick()) {
				region.setTime(TimeType.next(region.getTime()));
			}

			PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
			new RegionMenu(player, region);
		});

		gui.addItem(27, MenuUtils.getBackButton(), (_player, event) -> {
			if (!event.isLeftClick()) return;
			new RegionsMenu(player);
		});

		if (region.isPlayerMember(player)) {
			gui.addItem(35, MenuUtils.getButton(14, placeholder), (_player, event) -> {
				if (!event.isLeftClick()) return;

				region.removeMember(player);
				PlayerSound.play(player, PlayerSound.PredefinedSound.SUCCESS);
				RegionManager.addNewLog(region.getUniqueId(), 4, new Placeholder()
						.add("{playername}", player.getName()));
				new RegionsMenu(player);
			});
		}

		gui.open(player, MenuUtils.getEmptySlot());
	}
}