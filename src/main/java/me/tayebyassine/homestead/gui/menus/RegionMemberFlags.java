package me.tayebyassine.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.FlagCalculator;
import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionMember;
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

public final class RegionMemberFlags {
	private static final int BULK_INDEX = 0;

	public RegionMemberFlags(Player player, Region region, RegionMember member) {
		PaginationMenu.builder(
						MenuUtility.getTitle(6).replace("{playername}", member.getPlayerName()),
						9 * 5)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(buildItemsList(member))
				.fillEmptySlots()
				.goBack((_player, event) -> new RegionMembersMenu(player, region))
				.onClick((_player, context) -> handleFlagClick(player, region, member, context))
				.build()
				.open(player);
	}

	private void handleFlagClick(Player player, Region region, RegionMember member, PaginationMenu.ClickContext context) {
		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		if (!MemberManager.isMemberOfRegion(region.getUniqueId(), member.getPlayerId())) {
			player.closeInventory();
			return;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE)) return;

		if (!player.hasPermission("homestead.actions.regions.update.flags.members")) {
			Messages.send(player, "common.no_permission");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player, ControlFlag.SET_MEMBER_FLAGS.getBitmask())) {
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		if (player.getUniqueId().equals(member.getPlayerId())) {
			Messages.send(player, "common.no_permission");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		int index = context.getIndex();

		if (index == BULK_INDEX) {
			handleBulkToggle(player, region, member, context);
			return;
		}

		handleSingleToggle(player, region, member, context, index);
	}

	private void handleBulkToggle(Player player, Region region, RegionMember member, PaginationMenu.ClickContext context) {
		boolean enableAll = context.getEvent().isLeftClick();
		boolean disableAll = context.getEvent().isRightClick();
		if (!enableAll && !disableAll) return;

		long newFlags = member.getPlayerFlags();
		int changed = 0;

		for (String flagString : PlayerFlag.getFlags()) {
			if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) continue;

			long flag = PlayerFlag.parse(flagString);
			boolean isSet = FlagCalculator.isFlagSet(newFlags, flag);

			if (enableAll && !isSet) {
				newFlags = FlagCalculator.addFlag(newFlags, flag);
				changed++;
			} else if (disableAll && isSet) {
				newFlags = FlagCalculator.removeFlag(newFlags, flag);
				changed++;
			}
		}

		if (changed == 0) {
			Messages.send(player, "commands.flags.13");
			return;
		}

		member.setPlayerFlags(newFlags);
		PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
		context.getInstance().setItems(buildItemsList(member));
		Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);
	}

	private void handleSingleToggle(Player player, Region region, RegionMember member, PaginationMenu.ClickContext context, int index) {
		int flagListIndex = index - 1;
		if (flagListIndex < 0 || flagListIndex >= PlayerFlag.getFlags().size()) return;

		String flagString = PlayerFlag.getFlags().get(flagListIndex);

		if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			Messages.send(player, "commands.flags.9");
			return;
		}

		if (!context.getEvent().isLeftClick()) return;

		long flags = member.getPlayerFlags();
		long flag = PlayerFlag.parse(flagString);
		boolean isSet = FlagCalculator.isFlagSet(flags, flag);

		Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

		member.setPlayerFlags(isSet
				? FlagCalculator.removeFlag(flags, flag)
				: FlagCalculator.addFlag(flags, flag));

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE, flagString, member.getPlayerName(), Formatter.getFlagState(!isSet));

		PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
		context.getInstance().replaceSlot(index, MenuUtility.getFlagButton(flagString, !isSet));
	}

	private List<ItemStack> buildItemsList(RegionMember member) {
		List<ItemStack> items = new ArrayList<>();
		items.add(MenuUtility.getButton(65));

		for (String flagString : PlayerFlag.getFlags()) {
			boolean value = FlagCalculator.isFlagSet(member.getPlayerFlags(), PlayerFlag.parse(flagString));
			items.add(MenuUtility.getFlagButton(flagString, value));
		}

		return items;
	}
}


