package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
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

public final class RegionMemberControlFlags {

	public RegionMemberControlFlags(Player player, Region region, RegionMember member) {
		List<ItemStack> items = new ArrayList<>();

		for (String flagString : RegionControlFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(member.getControlFlags(), RegionControlFlags.valueOf(flagString));
			items.add(MenuUtility.getFlagButton(flagString, value));
		}

		PaginationMenu.builder(
						MenuUtility.getTitle(7).replace("{playername}", member.getPlayerName()),
						9 * 5)
				.nextPageItem(MenuUtility.getNextPageButton())
				.prevPageItem(MenuUtility.getPreviousPageButton())
				.items(items)
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

		if (!MemberManager.isMemberOfRegion(region.getUniqueId(), member.getPlayerId())) {
			player.closeInventory();
			return;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE)) return;

		if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, 159);
			return;
		}

		if (!player.hasPermission("homestead.region.flags.members")) {
			Messages.send(player, 8);
			return;
		}

		String flagString = RegionControlFlags.getFlags().get(context.getIndex());

		if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			Messages.send(player, 42);
			return;
		}

		if (!context.getEvent().isLeftClick()) return;

		long flags = member.getControlFlags();
		long flag = RegionControlFlags.valueOf(flagString);
		boolean isSet = FlagsCalculator.isFlagSet(flags, flag);

		Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

		member.setControlFlags(isSet
				? FlagsCalculator.removeFlag(flags, flag)
				: FlagsCalculator.addFlag(flags, flag));

		PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

		context.getInstance().replaceSlot(context.getIndex(), MenuUtility.getFlagButton(flagString, !isSet));
	}
}