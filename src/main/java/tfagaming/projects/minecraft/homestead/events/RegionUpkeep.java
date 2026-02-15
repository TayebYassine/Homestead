package tfagaming.projects.minecraft.homestead.events;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.other.UpkeepUtils;

public final class RegionUpkeep {
	private RegionUpkeep() {
	}

	/**
	 * Trigger event for: Region Upkeep
	 * @param instance Homestead's instance
	 */
	public static void trigger(Homestead instance) {
		for (Region region : RegionsManager.getAll()) {
			if (System.currentTimeMillis() > region.getUpkeepAt()) {
				double amountToPay = UpkeepUtils.getAmountToPay(region);

				if (amountToPay > region.getBank()) {
					int chunksToRemove = UpkeepUtils.getChunksToRemove(region);

					if (chunksToRemove > 0) {
						for (int i = 0; i < chunksToRemove; i++) {
							ChunksManager.removeRandomChunk(region.getUniqueId());
						}
					}

					if (region.getOwner().isOnline()) {
						Player regionOwner = (Player) region.getOwner();

						Placeholder placeholder = new Placeholder()
							.add("{amount}", Formatters.getBalance(amountToPay))
							.add("{region}", region.getName())
							.add("{chunks}", String.valueOf(chunksToRemove));

						Messages.send(regionOwner, 111, placeholder);
						Messages.send(regionOwner, 112, placeholder);
					}
				} else {
					region.withdrawBank(amountToPay);

					region.setUpkeepAt(UpkeepUtils.getNewUpkeepAt());

					if (region.getOwner().isOnline()) {
						Player regionOwner = (Player) region.getOwner();

						Placeholder placeholder = new Placeholder()
							.add("{amount}", Formatters.getBalance(amountToPay))
							.add("{region}", region.getName())
							.add("{bank}", Formatters.getBalance(region.getBank()));

						Messages.send(regionOwner, 109, placeholder);
						Messages.send(regionOwner, 110, placeholder);
					}
				}
			}
		}
	}
}
