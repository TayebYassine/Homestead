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
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class MemberPlayerFlagsMenu {
	// index 0 = Bulk item; alle anderen Items sind die eigentlichen Flags (+1 Index-Offset)
	private static final int BULK_INDEX = 0;
	private final HashSet<UUID> cooldowns = new HashSet<>();

	public MemberPlayerFlagsMenu(Player player, Region region, SerializableMember member) {
		List<ItemStack> items = buildItemsList(member);

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(6).replace("{playername}", member.getBukkitOfflinePlayer().getName()),
				9 * 5,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				items,
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

					// === Bulk toggle item ===
					if (index == BULK_INDEX) {
						boolean enableAll = context.getEvent().isLeftClick();
						boolean disableAll = context.getEvent().isRightClick();

						if (!enableAll && !disableAll) return;

						@SuppressWarnings("unchecked")
						List<String> disabledFlags = Homestead.config.getStringList("disabled-flags");

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
							region.setMemberFlags(member, newFlags);

							PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

							// UI neu aufbauen
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

					// === Einzelnes Flag toggeln ===
					int flagListIndex = index - 1; // wegen Bulk-Item
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

						region.setMemberFlags(member, newFlags);

						PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

						cooldowns.add(player.getUniqueId());

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

		// Einzelne Flag-Buttons
		for (String flagString : PlayerFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(member.getFlags(), PlayerFlags.valueOf(flagString));
			items.add(MenuUtils.getFlagButton(flagString, value));
		}
		return items;
	}
}
