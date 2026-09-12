package me.tayebyassine.homestead.sessions;

import com.google.common.base.Function;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import me.tayebyassine.homestead.resources.files.LanguageFile;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.platform.PlatformBridge;
import me.tayebyassine.homestead.util.minecraft.threads.TaskHandle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A session that waits for a player to type text input in chat.
 *
 * <p>Sessions are created via the {@link Builder} and automatically
 * register as a Bukkit {@link Listener} to intercept the next chat
 * message. The session supports configurable prompt display (chat,
 * title, or action bar), input validation, a cancel keyword, and an
 * automatic timeout.
 * </p>
 *
 * <p>Only one session can be active per player at a time; creating a new
 * session replaces any previous one.
 * </p>
 */
public final class PlayerInputSession implements Listener {

    public static final Map<UUID, PlayerInputSession> SESSIONS = new ConcurrentHashMap<>();

    private final Homestead plugin;
    private final Player player;
    private final BiConsumer<Player, String> callback;
    private final Function<String, Boolean> validator;
    private final Consumer<Player> onCancel;
    private final int promptId;
    private final Placeholder promptPlaceholder;
    private final TaskHandle repeatTask;
    private final TaskHandle timeoutTask;
    private final String inputType;
    private boolean titleSent = false;

    private PlayerInputSession(Builder builder) {
        this.plugin = builder.plugin;
        this.player = builder.player;
        this.callback = builder.callback;
        this.validator = builder.validator;
        this.onCancel = builder.onCancel;
        this.promptId = builder.promptId;
        this.promptPlaceholder = builder.promptPlaceholder;
        this.inputType = getInputType();

        PlayerInputSession old = SESSIONS.put(player.getUniqueId(), this);
        if (old != null) old.internalDestroy();

        Bukkit.getPluginManager().registerEvents(this, plugin);

        switch (inputType) {
            case "title": {
                List<String> titleData = Resources.<LanguageFile>get(ResourceType.Language)
                        .getInputPromptTitle(String.valueOf(promptId));

                if (titleData.size() == 2) {
                    String t1 = titleData.getFirst();
                    String t2 = titleData.get(1);
                    int stayTicks = builder.timeoutSeconds * 20;
                    PlatformBridge.get().showTitle(player, Formatter.applyPlaceholders(t1, promptPlaceholder), Formatter.applyPlaceholders(t2, promptPlaceholder), 10, stayTicks, 20);
                    this.titleSent = true;
                }
                break;
            }
            case "chat": {
                String text = Resources.<LanguageFile>get(ResourceType.Language)
                        .getInputPromptChat(String.valueOf(promptId));
                PlatformBridge.get().sendMessage(player, Formatter.applyPlaceholders(text, promptPlaceholder));
                break;
            }
        }

        this.repeatTask = plugin.runAsyncTimerTask(() -> {
            if (inputType.equals("actionbar")) {
                String text = Resources.<LanguageFile>get(ResourceType.Language)
                        .getInputPromptActionbar(String.valueOf(promptId));
                PlatformBridge.get().sendActionBar(player, Formatter.applyPlaceholders(text, promptPlaceholder));
            }
        }, 1);

        this.timeoutTask = plugin.runAsyncTaskLater(this::internalDestroy, builder.timeoutSeconds);
    }

    /**
     * Check whether the given player is currently waiting for input.
     *
     * @param player the player to check
     * @return {@code true} if an active input session exists for the player
     */
    public static boolean isWaitingForInput(Player player) {
        return SESSIONS.containsKey(player.getUniqueId());
    }

    /**
     * Create a new builder for a player input session.
     *
     * @param plugin the plugin instance
     * @param player the player to request input from
     * @return a new builder instance
     */
    public static Builder builder(Homestead plugin, Player player) {
        return new Builder(plugin, player);
    }

    private void internalDestroy() {
        SESSIONS.remove(player.getUniqueId(), this);
        HandlerList.unregisterAll(this);
        if (repeatTask != null) repeatTask.cancel();
        if (timeoutTask != null) timeoutTask.cancel();

        if (titleSent) {
            PlatformBridge.get().showTitle(player, "", "", 0, 0, 0);
        }
    }

    private String getInputType() {
        return Resources.<ConfigFile>get(ResourceType.Config).getPlayerInputType();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!e.getPlayer().equals(player)) return;
        e.setCancelled(true);

        String msg = e.getMessage();
        if (msg.equalsIgnoreCase("cancel")) {
            plugin.runSyncTask(() -> {
                if (onCancel != null) onCancel.accept(player);
                internalDestroy();
            });
            return;
        }

        if (validator != null && validator.apply(msg)) {
            plugin.runSyncTask(() -> {
                callback.accept(player, msg);
                internalDestroy();
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().equals(player) && SESSIONS.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (e.getPlayer().equals(player)) {
            internalDestroy();
        }
    }

    /**
     * A builder for constructing {@link PlayerInputSession} instances with
     * a fluent API.
     */
    public static final class Builder {

        private final Homestead plugin;
        private final Player player;
        private BiConsumer<Player, String> callback;
        private Function<String, Boolean> validator;
        private Consumer<Player> onCancel;
        private int promptId;
        private Placeholder promptPlaceholder;
        private int timeoutSeconds = 60;

        private Builder(Homestead plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
        }

        /**
         * Set the callback invoked with the player's input.
         *
         * @param callback the callback receiving the player and their input
         * @return this builder
         */
        public Builder callback(BiConsumer<Player, String> callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Set a validator that filters acceptable input. The session is
         * only forwarded to the callback when this returns {@code true}.
         *
         * @param validator the input validator
         * @return this builder
         */
        public Builder validator(Function<String, Boolean> validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Set a callback invoked when the player types "cancel".
         *
         * @param onCancel the cancel callback
         * @return this builder
         */
        public Builder onCancel(Consumer<Player> onCancel) {
            this.onCancel = onCancel;
            return this;
        }

        /**
         * Set the prompt ID used to look up the display message.
         *
         * @param promptId the prompt identifier
         * @return this builder
         */
        public Builder prompt(int promptId) {
            this.promptId = promptId;
            return this;
        }

        /**
         * Set the prompt ID and placeholder values for the display message.
         *
         * @param promptId          the prompt identifier
         * @param promptPlaceholder the placeholder values to apply
         * @return this builder
         */
        public Builder prompt(int promptId, Placeholder promptPlaceholder) {
            this.promptId = promptId;
            this.promptPlaceholder = promptPlaceholder;
            return this;
        }

        /**
         * Set the timeout in seconds before the session is automatically
         * destroyed.
         *
         * @param seconds the timeout duration
         * @return this builder
         */
        public Builder timeout(int seconds) {
            this.timeoutSeconds = seconds;
            return this;
        }

        /**
         * Build the session. A callback must have been set via
         * {@link #callback(BiConsumer)}.
         *
         * @return the new player input session
         * @throws IllegalStateException if no callback was set
         */
        public PlayerInputSession build() {
            if (callback == null) {
                throw new IllegalStateException("Callback must be set");
            }

            return new PlayerInputSession(this);
        }
    }
}
