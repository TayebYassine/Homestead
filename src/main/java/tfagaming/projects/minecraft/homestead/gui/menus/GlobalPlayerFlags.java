package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class GlobalPlayerFlags {
	private final HashSet<UUID> cooldowns = new HashSet<>();

	public GlobalPlayerFlags(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (String flagString : PlayerFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(region.getPlayerFlags(), PlayerFlags.valueOf(flagString));
			items.add(MenuUtils.getFlagButton(flagString, value));
		}

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(2), 9 * 5,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				items,
				(_player, event) -> new RegionMenu(player, region),
				(_player, context) -> {
					if (cooldowns.contains(player.getUniqueId())) return;

					if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
							RegionControlFlags.SET_GLOBAL_FLAGS)) {
						return;
					}

					String flagString = PlayerFlags.getFlags().get(context.getIndex());

					if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
						Messages.send(player, 42);
						return;
					}

					if (!context.getEvent().isLeftClick()) return;

					long flags = region.getPlayerFlags();
					long flag = PlayerFlags.valueOf(flagString);
					boolean isSet = FlagsCalculator.isFlagSet(flags, flag);

					region.setPlayerFlags(isSet
							? FlagsCalculator.removeFlag(flags, flag)
							: FlagsCalculator.addFlag(flags, flag));

					PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

					cooldowns.add(player.getUniqueId());
					context.getInstance().replaceSlot(context.getIndex(), MenuUtils.getFlagButton(flagString, !isSet));

					Homestead.getInstance().runAsyncTaskLater(() -> cooldowns.remove(player.getUniqueId()), 1);
				});

		gui.open(player, MenuUtils.getEmptySlot());
	}
}