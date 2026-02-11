package tfagaming.projects.minecraft.homestead.tools.other;

import tfagaming.projects.minecraft.homestead.Homestead;

public class TaxesUtils {
	public static long getNewTaxesAt() {
		if (Homestead.config.getBoolean("taxes.enabled")) {
			return System.currentTimeMillis() + (Homestead.config.getInt("taxes.tax-timer") * 1000L);
		}

		return 0;
	}
}
