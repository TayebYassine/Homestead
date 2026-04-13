package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.*;

public final class RegionMemberControlFlags {
	private final HashSet<UUID> cooldowns = new HashSet<>();

	public RegionMemberControlFlags(Player player, Region region, SerializableMember member) {
		List<ItemStack> items = new ArrayList<>();

		for (String flagString : RegionControlFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(member.getRegionControlFlags(), RegionControlFlags.valueOf(flagString));
			items.add(MenuUtils.getFlagButton(flagString, value));
		}

		OfflinePlayer memberBukkit = member.bukkit();

		PaginationMenu gui = new PaginationMenu(
				MenuUtils.getTitle(7).replace("{playername}", memberBukkit == null ? "?" : Objects.requireNonNull(memberBukkit.getName())),
				9 * 5,
				MenuUtils.getNextPageButton(),
				MenuUtils.getPreviousPageButton(),
				items,
				(_player, event) -> new RegionMembersMenu(player, region),
				(_player, context) -> {
					if (cooldowns.contains(player.getUniqueId())) return;

					if (!PlayerUtils.isOperator(player) && !region.isOwner(player)) {
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

					long flags = member.getRegionControlFlags();
					long flag = RegionControlFlags.valueOf(flagString);
					boolean isSet = FlagsCalculator.isFlagSet(flags, flag);

					region.setMemberRegionControlFlags(member, isSet
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