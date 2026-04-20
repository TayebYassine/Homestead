package tfagaming.projects.minecraft.homestead.tools.minecraft.economy;

import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.LevelRewards;

public final class UpkeepUtility {
	private UpkeepUtility() {
	}

	public static long getNewUpkeepAt() {
		if (Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("upkeep.enabled")) {
			return System.currentTimeMillis() + (Resources.<RegionsFile>get(ResourceType.Regions).getInt("upkeep.upkeep-timer") * 1000L);
		}

		return 0;
	}

	public static double getAmountToPay(Region region) {
		double amountPerChunk = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("upkeep.per-chunk");

		double price = amountPerChunk * region.getChunks().size();
		int reduction = LevelRewards.getUpkeepReductionByLevel(region);

		return price - (price * (reduction / 100.0));
	}

	public static int getChunksToRemove(Region region) {
		if (region.getBank() >= getAmountToPay(region)) {
			return 0;
		}

		double amountPerChunk = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("upkeep.per-chunk");
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
