package tfagaming.projects.minecraft.homestead.tools.minecraft.economy;

import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;

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
