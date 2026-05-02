package tfagaming.projects.minecraft.homestead.discord;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.*;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;

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

	public void sendContent(@NotNull String message, Object... data) throws Exception {
		for (int i = 0; i < data.length; i++) {
			message = Formatter.applyPlaceholders(message, new Placeholder()
					.add("{" + i + "}", data[i])
			);
		}

		executePost("{\"content\": \"" + escapeJson(message) + "\"}");
	}

	public void sendContent(@NotNull String message) throws Exception {
		executePost("{\"content\": \"" + escapeJson(message) + "\"}");
	}

	public void sendEmbed(@Nullable String title, @NotNull String description, int color, String[][] fields) throws Exception {
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

		String payload = "{\"embeds\":[" + embedBuilder + "]}";
		executePost(payload);
	}

	private void executePost(String jsonPayload) throws Exception {
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

	public boolean getEventEnabled(String eventName) {
		return Resources.<ConfigFile>get(ResourceType.Config).getBoolean("discord.events." + eventName + ".enabled");
	}

	public String getEventMessage(String eventName) {
		return Resources.<ConfigFile>get(ResourceType.Config).getString("discord.events." + eventName + ".message");
	}

	public void callEventDiscordWebhook(APIEvent event) {
		try {
			switch (event) {
				case RegionCreateEvent e -> {
					if (getEventEnabled("region_create")) {
						Region region = e.getRegion();

						sendContent(getEventMessage("region_create"), region == null ? "?" : region.getName());
					}
				}
				case RegionDeleteEvent e -> {
					if (getEventEnabled("region_delete")) {
						Region region = e.getRegion();

						sendContent(getEventMessage("region_delete"), region == null ? "?" : region.getName());
					}
				}
				case RegionNameUpdateEvent e -> {
					if (getEventEnabled("region_rename")) {
						sendContent(getEventMessage("region_rename"), e.getOldName(), e.getNewName());
					}
				}
				case RegionDescriptionUpdateEvent e -> {
					if (getEventEnabled("region_description_update")) {
						sendContent(getEventMessage("region_description_update"), e.getOldDescription(), e.getNewDescription());
					}
				}
				case RegionChatEvent e -> {
					if (getEventEnabled("region_private_chat")) {
						Region region = e.getRegion();
						OfflinePlayer player = e.getPlayer();

						sendContent(getEventMessage("region_private_chat"), player.getName(), player.getUniqueId(), region == null ? "?" : region.getName(), e.getMessage());
					}
				}
				case RegionOwnerUpdateEvent e -> {
					if (getEventEnabled("region_owner_transfer")) {
						Region region = e.getRegion();
						OfflinePlayer oldOwner = e.getOldOwner();
						OfflinePlayer newOwner = e.getNewOwner();

						sendContent(getEventMessage("region_owner_transfer"), oldOwner == null ? "?" : oldOwner.getName(), newOwner == null ? "?" : newOwner.getName(), region == null ? "?" : region.getName());
					}
				}
				case PlayerMailEvent e -> {
					if (getEventEnabled("player_send_mail")) {
						Region region = e.getRegion();
						OfflinePlayer player = e.getPlayer();

						sendContent(getEventMessage("player_send_mail"), player.getName(), player.getUniqueId(), region == null ? "?" : region.getName(), e.getMessage());
					}
				}
				case PlayerJoinRegionEvent e -> {
					if (getEventEnabled("player_join_region")) {
						Region region = e.getRegion();
						OfflinePlayer player = e.getPlayer();

						sendContent(getEventMessage("player_join_region"), player.getName(), player.getUniqueId(), region == null ? "?" : region.getName());
					}
				}
				case PlayerLeftRegionEvent e -> {
					if (getEventEnabled("player_left_region")) {
						Region region = e.getRegion();
						OfflinePlayer player = e.getPlayer();

						sendContent(getEventMessage("player_left_region"), player.getName(), player.getUniqueId(), region == null ? "?" : region.getName());
					}
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}
}