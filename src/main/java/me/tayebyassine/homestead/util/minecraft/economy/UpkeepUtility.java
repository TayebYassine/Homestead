package me.tayebyassine.homestead.util.minecraft.economy;

import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.minecraft.rewards.LevelRewards;

public final class UpkeepUtility {
    private UpkeepUtility() {
    }

    public static long getNewUpkeepAt() {
        if (Resources.<RegionsFile>get(ResourceType.Regions).isUpkeepEnabled()) {
            return System.currentTimeMillis() + (Resources.<RegionsFile>get(ResourceType.Regions).getUpkeepTimer() * 1000L);
        }

        return 0;
    }

    public static double getAmountToPay(Region region) {
        double amountPerChunk = Resources.<RegionsFile>get(ResourceType.Regions).getUpkeepPerChunk();

        double price = amountPerChunk * ChunkManager.getChunksOfRegion(region).size();
        int reduction = LevelRewards.getUpkeepReductionByLevel(region);

        return price - (price * (reduction / 100.0));
    }

    public static int getChunksToRemove(Region region) {
        if (region.getBank() >= getAmountToPay(region)) {
            return 0;
        }

        double amountPerChunk = Resources.<RegionsFile>get(ResourceType.Regions).getUpkeepPerChunk();
        double amountToPay = 0.0;
        int chunksToRemove = 1;

        for (int i = 0; i < ChunkManager.getChunksOfRegion(region).size(); i++) {
            if (amountToPay > region.getBank()) {
                chunksToRemove++;
            } else {
                amountToPay += amountPerChunk;
            }
        }

        return chunksToRemove;
    }
}
