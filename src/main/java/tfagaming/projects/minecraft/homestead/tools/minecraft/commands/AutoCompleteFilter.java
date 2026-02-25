package tfagaming.projects.minecraft.homestead.tools.minecraft.commands;

import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;

import java.util.ArrayList;
import java.util.List;

public final class AutoCompleteFilter {
	public static List<String> filter(List<String> suggestions, String[] args) {
		suggestions = ListUtils.removeDuplications(suggestions);

		List<String> filtered = new ArrayList<String>();

		for (String suggestion : suggestions) {
			if (suggestion == null) {
				continue;
			}

			if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
				filtered.add(suggestion);
			}
		}

		if (filtered.isEmpty()) {
			filtered.add("?");
		}

		return filtered;
	}
}
