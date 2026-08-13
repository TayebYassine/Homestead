package me.tayebyassine.homestead.tools.minecraft.economy;

import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;

public final class TaxesUtility {
	private TaxesUtility() {
	}

	public static long getNewTaxesAt() {
		if (Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("taxes.enabled")) {
			return System.currentTimeMillis() + (Resources.<RegionsFile>get(ResourceType.Regions).getInt("taxes.tax-timer") * 1000L);
		}

		return 0;
	}
}
