package me.tayebyassine.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.FlagsCalculator;
import me.tayebyassine.homestead.flags.ControlFlags;
import me.tayebyassine.homestead.flags.WorldFlags;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
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

public final class RegionWorldFlags {

	public RegionWorldFlags(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (String flagString : WorldFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(region.getWorldFlags(), WorldFlags.valueOf(flagString));
			items.add(MenuUtility.getFlagButton(flagString, value));
		}

		PaginationMenu.builder(3, 9 * 5)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(items)
				.fillEmptySlots()
				.goBack((_player, event) -> new RegionMenu(player, region))
				.onClick((_player, context) -> handleFlagClick(player, region, context))
				.build()
				.open(player);
	}

	private void handleFlagClick(Player player, Region region, PaginationMenu.ClickContext context) {
		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE)) return;

		if (!player.hasPermission("homestead.actions.regions.update.flags.world")) {
			Messages.send(player, "common.no_permission");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.SET_WORLD_FLAGS)) {
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		String flagString = WorldFlags.getFlags().get(context.getIndex());

		if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
			Messages.send(player, "commands.flags.9");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		if (!context.getEvent().isLeftClick()) return;

		long flags = region.getWorldFlags();
		long flag = WorldFlags.valueOf(flagString);

		OfflinePlayer offlineOwner = region.getOwner();
		Player owner = offlineOwner != null && offlineOwner.isOnline() ? (Player) offlineOwner : null;

		if (owner != null && Cooldown.hasCooldown(owner, Cooldown.Type.WAR_FLAG_DISABLED) && flag == WorldFlags.WARS) {
			Cooldown.sendCooldownMessage(player);
			return;
		}

		boolean isSet = FlagsCalculator.isFlagSet(flags, flag);

		Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

		region.setWorldFlags(isSet
				? FlagsCalculator.removeFlag(flags, flag)
				: FlagsCalculator.addFlag(flags, flag));

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE, flagString, region.getName(), Formatter.getFlagState(!isSet));

		PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

		context.getInstance().replaceSlot(context.getIndex(), MenuUtility.getFlagButton(flagString, !isSet));
	}
}