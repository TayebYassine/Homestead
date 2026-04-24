package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;


import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.*;

public final class RegionMemberFlags {
	/** Index 0 is the bulk-toggle item; all flag buttons start at index 1. */
	private static final int BULK_INDEX = 0;

	

	public RegionMemberFlags(Player player, Region region, SerializableMember member) {
		OfflinePlayer memberBukkit = member.bukkit();

		PaginationMenu gui = new PaginationMenu(
				MenuUtility.getTitle(6).replace("{playername}", memberBukkit == null ? "?" : Objects.requireNonNull(memberBukkit.getName())),
				9 * 5,
				MenuUtility.getNextPageButton(),
				MenuUtility.getPreviousPageButton(),
				buildItemsList(member),
				(_player, event) -> new RegionMembersMenu(player, region),
				(_player, context) -> {
					if (RegionManager.findRegion(region.getUniqueId()) == null) {
						player.closeInventory();
						return;
					}

					boolean stillMember = Objects.requireNonNull(RegionManager.findRegion(region.getUniqueId())).isPlayerMember(memberBukkit);

					if (!stillMember) {
						player.closeInventory();
						return;
					}

					if (Cooldown.hasCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE)) return;

					if (!player.hasPermission("homestead.region.flags.members")) {
						Messages.send(player, 8);
						return;
					}

					if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
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
							if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) continue;

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

						Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);
						return;
					}

					int flagListIndex = index - 1;
					if (flagListIndex < 0 || flagListIndex >= PlayerFlags.getFlags().size()) return;

					String flagString = PlayerFlags.getFlags().get(flagListIndex);

					if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
						PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
						Messages.send(player, 42);
						return;
					}

					if (!context.getEvent().isLeftClick()) return;

					long flags = member.getFlags();
					long flag = PlayerFlags.valueOf(flagString);
					boolean isSet = FlagsCalculator.isFlagSet(flags, flag);

					Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

					region.setMemberFlags(member, isSet
							? FlagsCalculator.removeFlag(flags, flag)
							: FlagsCalculator.addFlag(flags, flag));

					PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

					context.getInstance().replaceSlot(index, MenuUtility.getFlagButton(flagString, !isSet));
				});

		gui.open(player, MenuUtility.getEmptySlot());
	}

	private List<ItemStack> buildItemsList(SerializableMember member) {
		List<ItemStack> items = new ArrayList<>();
		items.add(MenuUtility.getButton(65)); // bulk-toggle item

		for (String flagString : PlayerFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(member.getFlags(), PlayerFlags.valueOf(flagString));
			items.add(MenuUtility.getFlagButton(flagString, value));
		}

		return items;
	}
}