package me.tayebyassine.homestead.discord;

import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.api.events.region.RegionCreateEvent;
import me.tayebyassine.homestead.api.events.region.RegionDeleteEvent;
import me.tayebyassine.homestead.api.events.region.RegionNameUpdateEvent;
import me.tayebyassine.homestead.api.events.region.RegionDisplaynameUpdateEvent;
import me.tayebyassine.homestead.api.events.region.RegionDescriptionUpdateEvent;
import me.tayebyassine.homestead.api.events.region.RegionOwnerUpdateEvent;
import me.tayebyassine.homestead.api.events.communication.RegionChatEvent;
import me.tayebyassine.homestead.api.events.communication.PlayerMailEvent;
import me.tayebyassine.homestead.api.events.player.PlayerJoinRegionEvent;
import me.tayebyassine.homestead.api.events.player.PlayerLeftRegionEvent;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

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
        return Resources.<ConfigFile>get(ResourceType.Config).isDiscordEventEnabled(eventName);
    }

    public String getEventMessage(String eventName) {
        return Resources.<ConfigFile>get(ResourceType.Config).getDiscordEventMessage(eventName);
    }

    public void callEventDiscordWebhook(APIEvent event) {
        try {
            switch (event) {
                case RegionCreateEvent e -> {
                    if (getEventEnabled("region_create")) {
                        Region region = e.getRegion();

                        sendContent(getEventMessage("region_create"), region.getName());
                    }
                }
                case RegionDeleteEvent e -> {
                    if (getEventEnabled("region_delete")) {
                        Region region = e.getRegion();

                        sendContent(getEventMessage("region_delete"), region.getName());
                    }
                }
                case RegionNameUpdateEvent e -> {
                    if (getEventEnabled("region_rename")) {
                        sendContent(getEventMessage("region_rename"), e.getOldName(), e.getNewName());
                    }
                }
                case RegionDisplaynameUpdateEvent e -> {
                    if (getEventEnabled("region_displayname_update")) {
                        sendContent(getEventMessage("region_displayname_update"), e.getOldDisplayname(), e.getNewDisplayname());
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

                        sendContent(getEventMessage("region_private_chat"), player.getName(), player.getUniqueId(), region.getName(), e.getMessage());
                    }
                }
                case RegionOwnerUpdateEvent e -> {
                    if (getEventEnabled("region_owner_transfer")) {
                        Region region = e.getRegion();
                        OfflinePlayer oldOwner = e.getOldOwner();
                        OfflinePlayer newOwner = e.getNewOwner();

                        sendContent(getEventMessage("region_owner_transfer"), oldOwner == null ? "?" : oldOwner.getName(), newOwner.getName(), region.getName());
                    }
                }
                case PlayerMailEvent e -> {
                    if (getEventEnabled("player_send_mail")) {
                        Region region = e.getRegion();
                        OfflinePlayer player = e.getPlayer();

                        sendContent(getEventMessage("player_send_mail"), player.getName(), player.getUniqueId(), region.getName(), e.getMessage());
                    }
                }
                case PlayerJoinRegionEvent e -> {
                    if (getEventEnabled("player_join_region")) {
                        Region region = e.getRegion();
                        OfflinePlayer player = e.getPlayer();

                        sendContent(getEventMessage("player_join_region"), player.getName(), player.getUniqueId(), region.getName());
                    }
                }
                case PlayerLeftRegionEvent e -> {
                    if (getEventEnabled("player_left_region")) {
                        Region region = e.getRegion();
                        OfflinePlayer player = e.getPlayer();

                        sendContent(getEventMessage("player_left_region"), player.getName(), player.getUniqueId(), region.getName());
                    }
                }
                default -> { /* do nothing */ }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
