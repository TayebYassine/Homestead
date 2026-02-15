package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class SubAreaFlagsMenu {
	private final HashSet<UUID> cooldowns = new HashSet<>();

	public SubAreaFlagsMenu(Player player, Region region, SubArea subArea) {
		List<ItemStack> items = new ArrayList<>();

		for (String flagString : PlayerFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(subArea.getFlags(), PlayerFlags.valueOf(flagString));

			items.add(MenuUtils.getFlagButton(flagString, value));
		}

		PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(16), 9 * 5,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(), items, (_player, event) -> {
			new SubAreaSettingsMenu(player, region, subArea);
		}, (_player, context) -> {
			if (cooldowns.contains(player.getUniqueId())) {
				return;
			}

			if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
					RegionControlFlags.MANAGE_SUBAREAS)) {
				return;
			}

			String flagString = PlayerFlags.getFlags().get(context.getIndex());

			if (Homestead.config.isFlagDisabled(flagString)) {
				Messages.send(player, 42);
				return;
			}

			long flag = PlayerFlags.valueOf(flagString);

			if (context.getEvent().isLeftClick()) {
				PaginationMenu instance = context.getInstance();

				long flags = subArea.getFlags();

				boolean isSet = FlagsCalculator.isFlagSet(flags, flag);
				long newFlags;

				if (isSet) {
					newFlags = FlagsCalculator.removeFlag(flags, flag);
				} else {
					newFlags = FlagsCalculator.addFlag(flags, flag);
				}

				subArea.setFlags(newFlags);

				cooldowns.add(player.getUniqueId());

				instance.replaceSlot(context.getIndex(),
						MenuUtils.getFlagButton(flagString, !isSet));

				Homestead.getInstance().runAsyncTaskLater(() -> {
					cooldowns.remove(player.getUniqueId());
				}, 1);
			}
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}
}