package me.tayebyassine.homestead.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.economy.UpkeepUtility;

public final class RegionUpkeep {
	private RegionUpkeep() {
	}

	/**
	 * Trigger event for: Region Upkeep
	 * @param instance Homestead's instance
	 */
	public static void trigger(Homestead instance) {
		for (Region region : RegionManager.getAll()) {
			if (System.currentTimeMillis() > region.getUpkeepAt()) {
				double amountToPay = UpkeepUtility.getAmountToPay(region);

				if (amountToPay > region.getBank()) {
					int chunksToRemove = UpkeepUtility.getChunksToRemove(region);

					if (chunksToRemove > 0) {
						for (int i = 0; i < chunksToRemove; i++) {
							ChunkManager.removeRandomChunk(region.getUniqueId());
						}
					}

					OfflinePlayer owner = region.getOwner();

					if (owner != null && owner.isOnline()) {
						Player regionOwner = (Player) region.getOwner();

						Placeholder placeholder = new Placeholder()
								.add("{amount}", Formatter.getBalance(amountToPay))
								.add("{region}", region.getName())
								.add("{chunks}", String.valueOf(chunksToRemove));

						Messages.send(regionOwner, "common.upkeep_error_cannot_pay", placeholder);
					}
				} else {
					region.withdrawBank(amountToPay);

					region.setUpkeepAt(UpkeepUtility.getNewUpkeepAt());

					OfflinePlayer owner = region.getOwner();

					if (owner != null && owner.isOnline()) {
						Player regionOwner = (Player) region.getOwner();

						Placeholder placeholder = new Placeholder()
								.add("{amount}", Formatter.getBalance(amountToPay))
								.add("{region}", region.getName())
								.add("{bank}", Formatter.getBalance(region.getBank()));

						Messages.send(regionOwner, "common.upkeep_success", placeholder);
					}
				}
			}
		}
	}
}
