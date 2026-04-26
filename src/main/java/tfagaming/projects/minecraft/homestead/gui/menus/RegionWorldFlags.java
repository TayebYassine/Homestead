package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;

import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public final class RegionWorldFlags {

	public RegionWorldFlags(Player player, Region region) {
		List<ItemStack> items = new ArrayList<>();

		for (String flagString : WorldFlags.getFlags()) {
			boolean value = FlagsCalculator.isFlagSet(region.getWorldFlags(), WorldFlags.valueOf(flagString));
			items.add(MenuUtility.getFlagButton(flagString, value));
		}

		PaginationMenu gui = new PaginationMenu(
				MenuUtility.getTitle(3), 9 * 5,
				MenuUtility.getNextPageButton(),
				MenuUtility.getPreviousPageButton(),
				items,
				(_player, event) -> new RegionMenu(player, region),
				(_player, context) -> {
					if (RegionManager.findRegion(region.getUniqueId()) == null) {
						player.closeInventory();
						return;
					}

					if (Cooldown.hasCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE)) return;

					if (!player.hasPermission("homestead.region.flags.world")) {
						Messages.send(player, 8);
						return;
					}

					if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
							RegionControlFlags.SET_WORLD_FLAGS)) {
						return;
					}

					String flagString = WorldFlags.getFlags().get(context.getIndex());

					if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagString)) {
						PlayerSound.play(player, PlayerSound.PredefinedSound.DENIED);
						Messages.send(player, 42);
						return;
					}

					if (!context.getEvent().isLeftClick()) return;

					long flags = region.getWorldFlags();
					long flag = WorldFlags.valueOf(flagString);

					if (Cooldown.hasCooldown(region.getOwner().getPlayer(), Cooldown.Type.WAR_FLAG_DISABLED) && flag == WorldFlags.WARS) {
						Cooldown.sendCooldownMessage(player);
						return;
					}

					boolean isSet = FlagsCalculator.isFlagSet(flags, flag);

					Cooldown.startCooldown(player, Cooldown.Type.FLAG_CHANGE_STATE);

					region.setWorldFlags(isSet
							? FlagsCalculator.removeFlag(flags, flag)
							: FlagsCalculator.addFlag(flags, flag));

					PlayerSound.play(player, PlayerSound.PredefinedSound.CLICK);

					context.getInstance().replaceSlot(context.getIndex(), MenuUtility.getFlagButton(flagString, !isSet));
				});

		gui.open(player, MenuUtility.getEmptySlot());
	}
}