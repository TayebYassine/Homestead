package me.tayebyassine.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.FlagsCalculator;
import me.tayebyassine.homestead.flags.ControlFlags;
import me.tayebyassine.homestead.gui.PaginationMenu;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionMember;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.FlagsFile;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;
import me.tayebyassine.homestead.tools.minecraft.menus.MenuUtility;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerSound;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public final class RegionMemberControlFlags {

	public RegionMemberControlFlags(Player player, Region region, RegionMember member) {
		List<ItemStack> items = new ArrayList<>();

		for (String flagString : ControlFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(member.getControlFlags(), ControlFlags.valueOf(flagString));
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

		if (!player.hasPermission("homestead.actions.regions.update.flags.members")) {
			Messages.send(player, "common.no_permission");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, "common.no_permission");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		String flagString = ControlFlags.getFlags().get(context.getIndex());

		if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
			Messages.send(player, "commands.flags.9");
			PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
			return;
		}

		if (!context.getEvent().isLeftClick()) return;

		long flags = member.getControlFlags();
		long flag = ControlFlags.valueOf(flagString);
		boolean isSet = FlagsCalculator.isFlagSet(flags, flag);

		Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

		member.setControlFlags(isSet
				? FlagsCalculator.removeFlag(flags, flag)
				: FlagsCalculator.addFlag(flags, flag));

		PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

		context.getInstance().replaceSlot(context.getIndex(), MenuUtility.getFlagButton(flagString, !isSet));
	}
}