package tfagaming.projects.minecraft.homestead.tools.java;

import tfagaming.projects.minecraft.homestead.logs.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class ListUtils {
	public static <T> List<T> removeDuplications(List<T> list) {
		return new ArrayList<>(new LinkedHashSet<>(list));
	}

	public static <T> List<T> removeNullElements(List<T> list) {
		List<T> newList = new ArrayList<>();

		for (T element : list) {
			if (element != null) {
				newList.add(element);
			}
		}

		return newList;
	}

	public static void printTable(String[] headers, Object[][] data) {
		int[] widths = new int[headers.length];
		for (int i = 0; i < headers.length; i++) {
			widths[i] = headers[i].length();
			for (Object[] row : data) {
				widths[i] = Math.max(widths[i], row[i].toString().length());
			}
		}

		StringBuilder output = new StringBuilder();

		for (int i = 0; i < headers.length; i++) {
			output.append(String.format("%-" + (widths[i] + 2) + "s", headers[i]));
		}
		Logger.info(output.toString());

		output = new StringBuilder();
		for (int width : widths) {
			output.append(String.format("%-" + (width + 2) + "s", "").replace(' ', '-'));
		}
		Logger.info(output.toString());

		for (Object[] row : data) {
			output = new StringBuilder();
			for (int i = 0; i < row.length; i++) {
				output.append(String.format("%-" + (widths[i] + 2) + "s", row[i]));
			}
			Logger.info(output.toString());
		}
	}
}
