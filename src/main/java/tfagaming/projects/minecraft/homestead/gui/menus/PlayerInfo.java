package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;

public final class PlayerInfo {
	public PlayerInfo(Player player, OfflinePlayer target, Runnable backButton) {
		Placeholder placeholder = new Placeholder()
				.add("{regions-count}", RegionManager.getRegionsOwnedByPlayer(target).size()
						+ RegionManager.getRegionsHasPlayerAsMember(target).size())
				.add("{playername}", target.getName())
				.add("{player-status}", Formatter.getPlayerStatus(target))
				.add("{player-balance}", Formatter.getBalance(PlayerBank.get(target)))
				.add("{player-ping}", target.isOnline() ? ((Player) target).getPing() : 0)
				.add("{player-joinedat}", Formatter.getDate(target.getFirstPlayed()))
				.add("{player-owned-regions}", Formatter.getPlayerOwnedRegions(target))
				.add("{player-trusted-regions}", Formatter.getPlayerTrustedRegions(target));

		Menu.builder(27, 9 * 3)
				.item(11, MenuUtility.getButton(21, placeholder, target))
				.item(13, MenuUtility.getButton(22, placeholder))
				.item(15, MenuUtility.getButton(23, placeholder))
				.button(18, MenuUtility.getBackButton(), (_player, event) -> {
					if (!event.isLeftClick()) return;
					backButton.run();
				})
				.build()
				.fillEmptySlots()
				.open(player);
	}
}