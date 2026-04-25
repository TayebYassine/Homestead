package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.War;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;


import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.items.ItemUtility;

import java.util.List;
import java.util.Objects;

public final class PlayerDeathListener implements Listener {
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player victim = event.getEntity();

		List<Region> ownedRegions = RegionManager.getRegionsOwnedByPlayer(victim);

		for (Region region : ownedRegions) {
			if (!WarManager.isRegionInWar(region.getUniqueId())) {
				continue;
			}

			War war = WarManager.findWarByRegion(region.getUniqueId());

			final List<OfflinePlayer> warMembers = List.copyOf(WarManager.getMembersOfWar(war.getUniqueId()));

			war = WarManager.removeRegionFromWar(region.getUniqueId());

			if (war == null) {
				continue;
			}

			Region winner = war.getWinner();

			Cooldown.startCooldown(victim, Cooldown.Type.WAR_FLAG_DISABLED);

			if (winner != null) {
				distributePrize(war, region, winner);
				giveHeadToWinner(winner, victim);

				OfflinePlayer winnerOwner = winner.getOwner();

				if (winnerOwner != null && winnerOwner.isOnline()) {
					Cooldown.startCooldown(Objects.requireNonNull(winnerOwner.getPlayer()), Cooldown.Type.WAR_FLAG_DISABLED);
				}
			}

			WarManager.tellPlayersWarEnded(warMembers, winner);

			WarManager.endWar(war.getUniqueId());

			Messages.send(victim, 163);

			boolean keepInventory = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("wars.keep-inventory");

			if (keepInventory) {
				event.setKeepInventory(true);
				event.getDrops().clear();
			} else {
				if (event.getKeepInventory()) {
					for (ItemStack item : victim.getInventory().getContents()) {
						if (item != null) event.getDrops().add(item);
					}
				}
				event.setKeepInventory(false);
			}

			break;
		}
	}

	private void distributePrize(War war, Region loser, Region winner) {
		double prize = war.getPrize();

		if (prize <= 0) {
			return;
		}

		double available = Math.min(prize, loser.getBank());

		if (available > 0) {
			loser.withdrawBank(available);
			winner.depositBank(available);
		}

		if (winner.getOwner().isOnline()) {
			Messages.send((Player) winner.getOwner(), 155);
		}
	}

	private void giveHeadToWinner(Region winner, Player victim) {
		if (!Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("wars.give-head")) {
			return;
		}

		if (!winner.getOwner().isOnline()) {
			return;
		}

		Player winnerOwner = (Player) winner.getOwner();
		ItemStack head = ItemUtility.getPlayerHead(victim);

		if (winnerOwner.getInventory().firstEmpty() == -1) {
			winnerOwner.getWorld().dropItemNaturally(winnerOwner.getLocation(), head);
		} else {
			winnerOwner.getInventory().addItem(head);
		}
	}
}