package tfagaming.projects.minecraft.homestead.gui.menus;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;

public class PlayerInfo {
	public PlayerInfo(Player player, OfflinePlayer target, Runnable backButton) {
		Menu gui = new Menu(MenuUtils.getTitle(27), 9 * 3);

		Placeholder placeholder = new Placeholder()
				.add("{regions-count}", RegionsManager.getRegionsOwnedByPlayer(target).size()
						+ RegionsManager.getRegionsHasPlayerAsMember(target).size())
				.add("{playername}", target.getName())
				.add("{player-status}", Formatter.getPlayerStatus(target))
				.add("{player-balance}", Formatter.getBalance(PlayerBank.get(target)))
				.add("{player-ping}", target.isOnline() ? ((Player) target).getPing() : 0)
				.add("{player-joinedat}", Formatter.getDate(target.getFirstPlayed()))
				.add("{player-owned-regions}", Formatter.getPlayerOwnedRegions(target))
				.add("{player-trusted-regions}", Formatter.getPlayerTrustedRegions(target));

		gui.addItem(11, MenuUtils.getButton(21, placeholder, target), null);
		gui.addItem(13, MenuUtils.getButton(22, placeholder), null);
		gui.addItem(15, MenuUtils.getButton(23, placeholder), null);

		gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
			if (!event.isLeftClick()) return;
			backButton.run();
		});

		gui.open(player, MenuUtils.getEmptySlot());
	}
}