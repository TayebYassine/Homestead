package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.*;

public class RegionMemberFlags {
	/** Index 0 is the bulk-toggle item; all flag buttons start at index 1. */
	private static final int BULK_INDEX = 0;

	private final HashSet<UUID> cooldowns = new HashSet<>();

	public RegionMemberFlags(Player player, Region region, SerializableMember member) {
		OfflinePlayer memberBukkit = member.bukkit();

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(6).replace("{playername}", memberBukkit == null ? "?" : Objects.requireNonNull(memberBukkit.getName())),
				9 * 5,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				buildItemsList(member),
				(_player, event) -> new RegionMembersMenu(player, region),
				(_player, context) -> {
					if (cooldowns.contains(player.getUniqueId())) return;

					if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
							RegionControlFlags.SET_MEMBER_FLAGS)) {
						return;
					}
					if (player.getUniqueId().equals(member.getPlayerId())) {
						Messages.send(player, 159);
						return;
					}

					int index = context.getIndex();

					if (index == BULK_INDEX) {
						boolean enableAll = context.getEvent().isLeftClick();
						boolean disableAll = context.getEvent().isRightClick();
						if (!enableAll && !disableAll) return;

						long newFlags = member.getFlags();
						int changed = 0;

						for (String flagString : PlayerFlags.getFlags()) {
							if (Homestead.config.isFlagDisabled(flagString)) continue;

							long flag = PlayerFlags.valueOf(flagString);
							boolean isSet = FlagsCalculator.isFlagSet(newFlags, flag);

							if (enableAll && !isSet) {
								newFlags = FlagsCalculator.addFlag(newFlags, flag);
								changed++;
							} else if (disableAll && isSet) {
								newFlags = FlagsCalculator.removeFlag(newFlags, flag);
								changed++;
							}
						}

						if (changed == 0) {
							Messages.send(player, 162);
							return;
						}

						region.setMemberFlags(member, newFlags);
						PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
						context.getInstance().setItems(buildItemsList(member));

						cooldowns.add(player.getUniqueId());
						Homestead.getInstance().runAsyncTaskLater(() -> cooldowns.remove(player.getUniqueId()), 1);
						return;
					}

					int flagListIndex = index - 1;
					if (flagListIndex < 0 || flagListIndex >= PlayerFlags.getFlags().size()) return;

					String flagString = PlayerFlags.getFlags().get(flagListIndex);

					if (Homestead.config.isFlagDisabled(flagString)) {
						Messages.send(player, 42);
						return;
					}

					if (!context.getEvent().isLeftClick()) return;

					long flags = member.getFlags();
					long flag = PlayerFlags.valueOf(flagString);
					boolean isSet = FlagsCalculator.isFlagSet(flags, flag);

					region.setMemberFlags(member, isSet
							? FlagsCalculator.removeFlag(flags, flag)
							: FlagsCalculator.addFlag(flags, flag));

					PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

					cooldowns.add(player.getUniqueId());
					context.getInstance().replaceSlot(index, MenuUtils.getFlagButton(flagString, !isSet));

					Homestead.getInstance().runAsyncTaskLater(() -> cooldowns.remove(player.getUniqueId()), 1);
				});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	private List<ItemStack> buildItemsList(SerializableMember member) {
		List<ItemStack> items = new ArrayList<>();
		items.add(MenuUtils.getButton(65)); // bulk-toggle item

		for (String flagString : PlayerFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(member.getFlags(), PlayerFlags.valueOf(flagString));
			items.add(MenuUtils.getFlagButton(flagString, value));
		}

		return items;
	}
}