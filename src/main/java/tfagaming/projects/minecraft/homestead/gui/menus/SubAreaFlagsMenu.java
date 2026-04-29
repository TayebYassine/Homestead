package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.SubArea;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public final class SubAreaFlagsMenu {

	public SubAreaFlagsMenu(Player player, Region region, SubArea subArea) {
		List<ItemStack> items = new ArrayList<>();

		for (String flagString : PlayerFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(subArea.getPlayerFlags(), PlayerFlags.valueOf(flagString));
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

		if (!player.hasPermission("homestead.region.subareas.flags")) {
			Messages.send(player, 8);
			return;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.MANAGE_SUBAREAS)) {
			return;
		}

		String flagString = PlayerFlags.getFlags().get(context.getIndex());

		if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			Messages.send(player, 42);
			return;
		}

		if (!context.getEvent().isLeftClick()) return;

		long flags = subArea.getPlayerFlags();
		long flag = PlayerFlags.valueOf(flagString);
		boolean isSet = FlagsCalculator.isFlagSet(flags, flag);

		Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

		subArea.setPlayerFlags(isSet
				? FlagsCalculator.removeFlag(flags, flag)
				: FlagsCalculator.addFlag(flags, flag));

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE, flagString, subArea.getName(), Formatter.getFlagState(!isSet));

		context.getInstance().replaceSlot(context.getIndex(), MenuUtility.getFlagButton(flagString, !isSet));
	}
}