package me.tayebyassine.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.FlagsCalculator;
import me.tayebyassine.homestead.flags.PlayerFlags;
import me.tayebyassine.homestead.flags.ControlFlags;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.FlagsFile;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;
import me.tayebyassine.homestead.tools.minecraft.menus.MenuUtility;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public final class GlobalPlayerFlags {

	public GlobalPlayerFlags(Player player, Region region) {
		List<ItemStack> flagItems = new ArrayList<>();

		for (String flagString : PlayerFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(region.getPlayerFlags(), PlayerFlags.valueOf(flagString));
			flagItems.add(MenuUtility.getFlagButton(flagString, value));
		}

		PaginationMenu gui = PaginationMenu.builder(2, 9 * 5)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(flagItems)
				.fillEmptySlots()
				.goBack((_player, event) -> new RegionMenu(player, region))
				.onClick((_player, context) -> handleFlagClick(player, region, context))
				.build();

		gui.open(player);
	}

	private void handleFlagClick(Player player, Region region, PaginationMenu.ClickContext context) {
		if (!context.getEvent().isLeftClick()) return;

		if (RegionManager.findRegion(region.getUniqueId()) == null) {
			player.closeInventory();
			return;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE)) return;

		if (!player.hasPermission("homestead.actions.regions.update.flags.global")) {
			Messages.send(player, "common.no_permission");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.SET_GLOBAL_FLAGS)) {
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		String flagString = PlayerFlags.getFlags().get(context.getIndex());

		if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
			Messages.send(player, "commands.flags.9");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		long flags = region.getPlayerFlags();
		long flag = PlayerFlags.valueOf(flagString);
		boolean isSet = FlagsCalculator.isFlagSet(flags, flag);

		Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

		region.setPlayerFlags(isSet
				? FlagsCalculator.removeFlag(flags, flag)
				: FlagsCalculator.addFlag(flags, flag));

		PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

		context.getInstance().replaceSlot(context.getIndex(), MenuUtility.getFlagButton(flagString, !isSet));
	}
}