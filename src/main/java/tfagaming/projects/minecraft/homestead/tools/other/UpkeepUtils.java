package tfagaming.projects.minecraft.homestead.tools.other;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.LevelsManager;
import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.LevelRewards;

public class UpkeepUtils {
	public static long getNewUpkeepAt() {
		if ((boolean) Homestead.config.get("upkeep.enabled")) {
			return System.currentTimeMillis() + ((int) Homestead.config.get("upkeep.upkeep-timer") * 1000);
		}

		return 0;
	}

	public static double getAmountToPay(Region region) {
		double amountPerChunk = Homestead.config.get("upkeep.per-chunk");

		double price = amountPerChunk * region.getChunks().size();
		int reduction = LevelRewards.getUpkeepReductionByLevel(region);

		double reductedPrice = price - (price * (reduction / 100.0));

		System.out.println("PRICE: " + price + ", REDUCTION: " + reduction + "%, REDUCTED PRICE: " + reductedPrice);
		return reductedPrice;
	}

	public static int getChunksToRemove(Region region) {
		if (region.getBank() >= getAmountToPay(region)) {
			return 0;
		}

		double amountPerChunk = Homestead.config.get("upkeep.per-chunk");
		double amountToPay = 0.0;
		int chunksToRemove = 1;

		for (int i = 0; i < region.getChunks().size(); i++) {
			if (amountToPay > region.getBank()) {
				chunksToRemove++;
			} else {
				amountToPay += amountPerChunk;
			}
		}

		return chunksToRemove;
	}
}
