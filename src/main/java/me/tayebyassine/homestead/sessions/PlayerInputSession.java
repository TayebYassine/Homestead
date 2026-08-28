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

public final class PlayerInputSession implements Listener {

    private static final Map<UUID, PlayerInputSession> SESSIONS = new ConcurrentHashMap<>();

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
                        .getStringList("input." + promptId + ".title");

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
                        .getString("input." + promptId + ".chat");
                PlatformBridge.get().sendMessage(player, Formatter.applyPlaceholders(text, promptPlaceholder));
                break;
            }
        }

        this.repeatTask = plugin.runAsyncTimerTask(() -> {
            if (inputType.equals("actionbar")) {
                String text = Resources.<LanguageFile>get(ResourceType.Language)
                        .getString("input." + promptId + ".actionbar");
                PlatformBridge.get().sendActionBar(player, Formatter.applyPlaceholders(text, promptPlaceholder));
            }
        }, 1);

        this.timeoutTask = plugin.runAsyncTaskLater(this::internalDestroy, builder.timeoutSeconds);
    }

    public static boolean isWaitingForInput(Player player) {
        return SESSIONS.containsKey(player.getUniqueId());
    }

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
        return Resources.<ConfigFile>get(ResourceType.Config).getString("player-input.type");
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

        public Builder callback(BiConsumer<Player, String> callback) {
            this.callback = callback;
            return this;
        }

        public Builder validator(Function<String, Boolean> validator) {
            this.validator = validator;
            return this;
        }

        public Builder onCancel(Consumer<Player> onCancel) {
            this.onCancel = onCancel;
            return this;
        }

        public Builder prompt(int promptId) {
            this.promptId = promptId;
            return this;
        }

        public Builder prompt(int promptId, Placeholder promptPlaceholder) {
            this.promptId = promptId;
            this.promptPlaceholder = promptPlaceholder;
            return this;
        }

        public Builder timeout(int seconds) {
            this.timeoutSeconds = seconds;
            return this;
        }

        public PlayerInputSession build() {
            if (callback == null) {
                throw new IllegalStateException("Callback must be set");
            }

            return new PlayerInputSession(this);
        }
    }
}