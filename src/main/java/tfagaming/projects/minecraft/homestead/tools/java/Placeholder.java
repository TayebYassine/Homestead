package tfagaming.projects.minecraft.homestead.tools.java;

import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.HashMap;
import java.util.Map;

public final class Placeholder {
	private final Map<String, Object> placeholders = new HashMap<>();

	{
		add("{__prefix__}", Homestead.config.getPrefix());
	}

	public Placeholder add(String key, Object value) {
		placeholders.put(key, value);

		return this;
	}

	public Map<String, String> build() {
		Map<String, String> result = new HashMap<>();

		for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
			result.put(entry.getKey(), entry.getValue() == null ? "NULL" : String.valueOf(entry.getValue()));
		}

		return result;
	}
}