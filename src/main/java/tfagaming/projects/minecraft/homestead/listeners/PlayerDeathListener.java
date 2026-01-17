package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.WarsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.List;

public final class PlayerDeathListener implements Listener {
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player victim = event.getEntity();

		List<Region> regions = RegionsManager.getRegionsOwnedByPlayer(victim);

		for (Region region : regions) {
			if (WarsManager.isRegionInWar(region.getUniqueId())) {
				War war = WarsManager.surrenderRegionFromFirstWarFound(region.getUniqueId());

				if (war != null && war.getRegions().size() == 1) {
					Region winner = war.getRegions().getFirst();

					double prize = war.getPrize();

					region.removeBalanceFromBank(prize);
					winner.addBalanceToBank(prize);

					if (winner.getOwner().isOnline()) {
						PlayerUtils.sendMessage((Player) winner.getOwner(), 155);
					}

					WarsManager.endWar(war.getUniqueId());
				}

				PlayerUtils.sendMessage(victim, 163);

				break;
			}
		}
	}
}
