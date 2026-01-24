package tfagaming.projects.minecraft.homestead.tools.java;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtils {
	public static double truncateToTwoDecimalPlaces(double number) {
		return Math.floor(number * 100) / 100;
	}

	public static String convertToBalance(double number) {
		NumberFormat formatter = NumberFormat.getCompactNumberInstance(
				Locale.US, NumberFormat.Style.SHORT);

		formatter.setMaximumFractionDigits(2);

		return formatter.format(number);
	}

	public static String convertToBalance(long number) {
		NumberFormat formatter = NumberFormat.getCompactNumberInstance(
				Locale.US, NumberFormat.Style.SHORT);

		formatter.setMaximumFractionDigits(2);

		return formatter.format(number);
	}

	public static boolean isValidDouble(String string) {
		try {
			Double.parseDouble(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isValidInteger(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isValidLong(String string) {
		try {
			Long.parseLong(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
