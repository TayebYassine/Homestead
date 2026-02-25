package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.List;

public final class PlayerDeathListener implements Listener {
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player victim = event.getEntity();

		List<Region> regions = RegionManager.getRegionsOwnedByPlayer(victim);

		for (Region region : regions) {
			if (WarManager.isRegionInWar(region.getUniqueId())) {
				War war = WarManager.surrenderRegionFromFirstWarFound(region.getUniqueId());

				if (war != null && war.getRegions().size() == 1) {
					Region winner = war.getRegions().getFirst();

					double prize = war.getPrize();

					region.withdrawBank(prize);
					winner.addBalanceToBank(prize);

					if (winner.getOwner().isOnline()) {
						Messages.send((Player) winner.getOwner(), 155);
					}

					WarManager.endWar(war.getUniqueId());
				}

				Messages.send(victim, 163);

				break;
			}
		}
	}
}
