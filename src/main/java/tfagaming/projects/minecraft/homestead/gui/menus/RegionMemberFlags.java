package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

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

		if (!MemberManager.isMemberOfRegion(region, player)) {
			player.closeInventory();
			return;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE)) return;

		if (!player.hasPermission("homestead.region.flags.members")) {
			Messages.send(player, 8);
			return;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player, RegionControlFlags.SET_MEMBER_FLAGS)) {
			return;
		}

		if (player.getUniqueId().equals(member.getPlayerId())) {
			Messages.send(player, 159);
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

		member.setPlayerFlags(newFlags);
		PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
		context.getInstance().setItems(buildItemsList(member));
		Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);
	}

	private void handleSingleToggle(Player player, Region region, RegionMember member, PaginationMenu.ClickContext context, int index) {
		int flagListIndex = index - 1;
		if (flagListIndex < 0 || flagListIndex >= PlayerFlags.getFlags().size()) return;

		String flagString = PlayerFlags.getFlags().get(flagListIndex);

		if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			Messages.send(player, 42);
			return;
		}

		if (!context.getEvent().isLeftClick()) return;

		long flags = member.getPlayerFlags();
		long flag = PlayerFlags.valueOf(flagString);
		boolean isSet = FlagsCalculator.isFlagSet(flags, flag);

		Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

		member.setPlayerFlags(isSet
				? FlagsCalculator.removeFlag(flags, flag)
				: FlagsCalculator.addFlag(flags, flag));

		PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);
		context.getInstance().replaceSlot(index, MenuUtility.getFlagButton(flagString, !isSet));
	}

	private List<ItemStack> buildItemsList(RegionMember member) {
		List<ItemStack> items = new ArrayList<>();
		items.add(MenuUtility.getButton(65));

		for (String flagString : PlayerFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(member.getPlayerFlags(), PlayerFlags.valueOf(flagString));
			items.add(MenuUtility.getFlagButton(flagString, value));
		}

		return items;
	}
}