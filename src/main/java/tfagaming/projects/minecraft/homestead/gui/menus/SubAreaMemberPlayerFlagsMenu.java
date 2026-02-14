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
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.*;

public class SubAreaMemberPlayerFlagsMenu {
	private static final int BULK_INDEX = 0;
	private final HashSet<UUID> cooldowns = new HashSet<>();

	public SubAreaMemberPlayerFlagsMenu(Player player, Region region, SubArea subArea, SerializableMember member) {
		List<ItemStack> items = buildItemsList(member);

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(25).replace("{playername}", Objects.requireNonNull(member.getBukkitOfflinePlayer().getName())),
				9 * 5,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				items,
				(_player, event) -> new SubAreaMembersMenu(player, region, subArea),
				(_player, context) -> {
					if (cooldowns.contains(player.getUniqueId())) return;

					if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
							RegionControlFlags.MANAGE_SUBAREAS)) {
						return;
					}

					int index = context.getIndex();

					// === Bulk toggle item ===
					if (index == BULK_INDEX) {
						boolean enableAll = context.getEvent().isLeftClick();
						boolean disableAll = context.getEvent().isRightClick();

						if (!enableAll && !disableAll) return;

						long current = member.getFlags();
						long newFlags = current;

						int changed = 0;
						for (String flagString : PlayerFlags.getFlags()) {
							if (Homestead.config.isFlagDisabled(flagString)) continue; // locked -> skip
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

						if (changed > 0) {
							subArea.setMemberFlags(member, newFlags);

							player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 500.0f, 1.0f);

							Map<String, String> replacements = new HashMap<>();
							replacements.put("{changes}", String.valueOf(changed));
							replacements.put("{new-value}", Formatters.getBoolean(enableAll));

							Messages.send(player, 161, replacements);

							PaginationMenu instance = context.getInstance();
							instance.setItems(buildItemsList(member));

							cooldowns.add(player.getUniqueId());
							Homestead.getInstance().runAsyncTaskLater(() ->
									cooldowns.remove(player.getUniqueId()), 1);
						} else {
							Messages.send(player, 162);
						}
						return;
					}

					int flagListIndex = index - 1;
					if (flagListIndex < 0 || flagListIndex >= PlayerFlags.getFlags().size()) return;

					String flagString = PlayerFlags.getFlags().get(flagListIndex);

					if (Homestead.config.isFlagDisabled(flagString)) {
						Messages.send(player, 42);
						return;
					}

					long flag = PlayerFlags.valueOf(flagString);

					if (context.getEvent().isLeftClick()) {
						PaginationMenu instance = context.getInstance();

						long flags = member.getFlags();

						boolean isSet = FlagsCalculator.isFlagSet(flags, flag);
						long newFlags;

						if (isSet) {
							newFlags = FlagsCalculator.removeFlag(flags, flag);
						} else {
							newFlags = FlagsCalculator.addFlag(flags, flag);
						}

						subArea.setMemberFlags(member, newFlags);

						cooldowns.add(player.getUniqueId());

						player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 500.0f, 1.0f);

						Map<String, String> replacements = new HashMap<>();
						replacements.put("{flag}", flagString);
						replacements.put("{state}", Formatters.getFlag(!isSet));
						replacements.put("{region}", region.getName());
						replacements.put("{subarea}", subArea.getName());
						replacements.put("{player}", member.getBukkitOfflinePlayer().getName());

						Messages.send(player, 169, replacements);

						instance.replaceSlot(index, MenuUtils.getFlagButton(flagString, !isSet));

						Homestead.getInstance().runAsyncTaskLater(() -> cooldowns.remove(player.getUniqueId()), 1);
					}
				});

		gui.open(player, MenuUtils.getEmptySlot());
	}

	private List<ItemStack> buildItemsList(SerializableMember member) {
		List<ItemStack> items = new ArrayList<>();

		ItemStack bulk = MenuUtils.getButton(65);

		items.add(bulk);

		for (String flagString : PlayerFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(member.getFlags(), PlayerFlags.valueOf(flagString));
			items.add(MenuUtils.getFlagButton(flagString, value));
		}
		return items;
	}
}
