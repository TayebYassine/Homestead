package me.tayebyassine.homestead.util.minecraft.economy;

import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;

public final class TaxesUtility {
    private TaxesUtility() {
    }

    public static long getNewTaxesAt() {
        if (Resources.<RegionsFile>get(ResourceType.Regions).isTaxesEnabled()) {
            return System.currentTimeMillis() + (Resources.<RegionsFile>get(ResourceType.Regions).getTaxTimer() * 1000L);
        }

        return 0;
    }
}
