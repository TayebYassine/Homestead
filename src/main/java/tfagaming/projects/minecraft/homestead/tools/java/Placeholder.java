package tfagaming.projects.minecraft.homestead.tools.java;

import java.util.HashMap;
import java.util.Map;

/**
 * Class holder for creating placeholders for a string.
 */
public final class Placeholder {
	private final Map<String, Object> placeholders = new HashMap<>();

	/**
	 * Add a replacement string to the list of placeholders.<br>
	 * Example usage: <pre>{@code
	 *  Placeholder plcd = new Placeholder();
	 *  plcd.add("{message}", "Hello World!");
	 *  plcd.add("{count}", 48);
	 *  plcd.add("{current-state}", true);
	 *  }</pre>
	 */
	public Placeholder add(String key, Object value) {
		placeholders.put(key, value);

		return this;
	}

	/**
	 * Convert the placeholder class to a {@code Map<String, String>}.
	 * If a placeholder value is {@code null}, it will replace it with {@code "NULL"} instead.
	 */
	public Map<String, String> build() {
		Map<String, String> result = new HashMap<>();

		for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
			result.put(entry.getKey(), entry.getValue() == null ? "NULL" : String.valueOf(entry.getValue()));
		}

		return result;
	}
}