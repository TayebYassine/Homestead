package tfagaming.projects.minecraft.homestead.events;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.economy.UpkeepUtility;

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

					if (region.getOwner().isOnline()) {
						Player regionOwner = (Player) region.getOwner();

						Placeholder placeholder = new Placeholder()
								.add("{amount}", Formatter.getBalance(amountToPay))
								.add("{region}", region.getName())
								.add("{chunks}", String.valueOf(chunksToRemove));

						Messages.send(regionOwner, 111, placeholder);
						Messages.send(regionOwner, 112, placeholder);
					}
				} else {
					region.withdrawBank(amountToPay);

					region.setUpkeepAt(UpkeepUtility.getNewUpkeepAt());

					if (region.getOwner().isOnline()) {
						Player regionOwner = (Player) region.getOwner();

						Placeholder placeholder = new Placeholder()
								.add("{amount}", Formatter.getBalance(amountToPay))
								.add("{region}", region.getName())
								.add("{bank}", Formatter.getBalance(region.getBank()));

						Messages.send(regionOwner, 109, placeholder);
						Messages.send(regionOwner, 110, placeholder);
					}
				}
			}
		}
	}
}
