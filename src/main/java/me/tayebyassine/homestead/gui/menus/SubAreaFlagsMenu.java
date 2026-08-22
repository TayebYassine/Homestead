package me.tayebyassine.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.FlagCalculator;
import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.FlagsFile;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.menus.MenuUtility;
import me.tayebyassine.homestead.util.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public final class SubAreaFlagsMenu {

	public SubAreaFlagsMenu(Player player, Region region, SubArea subArea) {
		List<ItemStack> items = new ArrayList<>();

		for (String flagString : PlayerFlag.getFlags()) {
			boolean value = FlagCalculator.isFlagSet(subArea.getPlayerFlags(), PlayerFlag.parse(flagString));
			items.add(MenuUtility.getFlagButton(flagString, value));
		}

		PaginationMenu.builder(16, 9 * 5)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(items)
				.fillEmptySlots()
				.goBack((_player, event) -> new SubAreaMenu(player, region, subArea))
				.onClick((_player, context) -> handleFlagClick(player, region, subArea, context))
				.build()
				.open(player);
	}

	private void handleFlagClick(Player player, Region region, SubArea subArea, PaginationMenu.ClickContext context) {
		if (RegionManager.findRegion(region.getUniqueId()) == null || SubAreaManager.findSubArea(subArea.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE)) return;

		if (!player.hasPermission("homestead.actions.regions.subareas.update.flags.global")) {
			Messages.send(player, "common.no_permission");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlag.MANAGE_SUBAREAS.getBitmask())) {
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		String flagString = PlayerFlag.getFlags().get(context.getIndex());

		if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
			Messages.send(player, "commands.flags.9");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		if (!context.getEvent().isLeftClick()) return;

		long flags = subArea.getPlayerFlags();
		long flag = PlayerFlag.parse(flagString);
		boolean isSet = FlagCalculator.isFlagSet(flags, flag);

		Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

		subArea.setPlayerFlags(isSet
				? FlagCalculator.removeFlag(flags, flag)
				: FlagCalculator.addFlag(flags, flag));

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE, flagString, subArea.getName(), Formatter.getFlagState(!isSet));

		context.getInstance().replaceSlot(context.getIndex(), MenuUtility.getFlagButton(flagString, !isSet));
	}
}


