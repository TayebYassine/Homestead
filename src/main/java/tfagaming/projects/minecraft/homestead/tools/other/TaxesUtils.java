package tfagaming.projects.minecraft.homestead.tools.other;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;

public class TaxesUtils {
	public static long getNewTaxesAt() {
		if (Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("taxes.enabled")) {
			return System.currentTimeMillis() + (Resources.<RegionsFile>get(ResourceType.Regions).getInt("taxes.tax-timer") * 1000L);
		}

		return 0;
	}
}
