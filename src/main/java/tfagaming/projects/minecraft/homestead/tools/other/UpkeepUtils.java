package tfagaming.projects.minecraft.homestead.tools.other;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.LevelRewards;

public class UpkeepUtils {
	public static long getNewUpkeepAt() {
		if (Homestead.config.getBoolean("upkeep.enabled")) {
			return System.currentTimeMillis() + (Homestead.config.getInt("upkeep.upkeep-timer") * 1000L);
		}

		return 0;
	}

	public static double getAmountToPay(Region region) {
		double amountPerChunk = Homestead.config.getDouble("upkeep.per-chunk");

		double price = amountPerChunk * region.getChunks().size();
		int reduction = LevelRewards.getUpkeepReductionByLevel(region);

		return price - (price * (reduction / 100.0));
	}

	public static int getChunksToRemove(Region region) {
		if (region.getBank() >= getAmountToPay(region)) {
			return 0;
		}

		double amountPerChunk = Homestead.config.getDouble("upkeep.per-chunk");
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
