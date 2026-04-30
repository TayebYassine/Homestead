package tfagaming.projects.minecraft.homestead.discord;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhookClient {
	private final String webhookUrl;

	public DiscordWebhookClient(String url) {
		this.webhookUrl = url;
	}

	public void sendContent(@NotNull String message) {
		executePost("{\"content\": \"" + escapeJson(message) + "\"}");
	}

	public void sendEmbed(@Nullable String title, @NotNull String description, int color, String[][] fields) {
		StringBuilder embedBuilder = new StringBuilder();
		embedBuilder.append("{");

		if (title != null) {
			embedBuilder.append("\"title\":\"").append(escapeJson(title)).append("\",");
		}

		embedBuilder.append("\"description\":\"").append(escapeJson(description)).append("\",");
		embedBuilder.append("\"color\":").append(color);

		if (fields != null && fields.length > 0) {
			embedBuilder.append(",\"fields\":[");
			for (int i = 0; i < fields.length; i++) {
				String[] field = fields[i];
				if (field.length >= 2) {
					if (i > 0) embedBuilder.append(",");
					embedBuilder.append("{");
					embedBuilder.append("\"name\":\"").append(escapeJson(field[0])).append("\",");
					embedBuilder.append("\"value\":\"").append(escapeJson(field[1])).append("\"");
					if (field.length >= 3 && "true".equalsIgnoreCase(field[2])) {
						embedBuilder.append(",\"inline\":true");
					}
					embedBuilder.append("}");
				}
			}
			embedBuilder.append("]");
		}

		embedBuilder.append("}");

		String payload = "{\"embeds\":[" + embedBuilder.toString() + "]}";
		executePost(payload);
	}

	private void executePost(String jsonPayload) {
		try {
			URL url = new URI(webhookUrl).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int responseCode = connection.getResponseCode();
			connection.disconnect();
		} catch (Exception ignored) {
		}
	}

	private String escapeJson(String input) {
		if (input == null) return "";
		return input
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}
}