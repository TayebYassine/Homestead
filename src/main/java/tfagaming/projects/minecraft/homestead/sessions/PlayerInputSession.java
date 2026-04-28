package tfagaming.projects.minecraft.homestead.sessions;

import com.google.common.base.Function;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.tools.minecraft.threads.TaskHandle;
import tfagaming.projects.minecraft.homestead.tools.minecraft.platform.PlatformBridge;

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
	private final String prompt;
	private final TaskHandle repeatTask;
	private final TaskHandle timeoutTask;

	private PlayerInputSession(Builder builder) {
		this.plugin = builder.plugin;
		this.player = builder.player;
		this.callback = builder.callback;
		this.validator = builder.validator;
		this.onCancel = builder.onCancel;
		this.prompt = builder.prompt;

		PlayerInputSession old = SESSIONS.put(player.getUniqueId(), this);
		if (old != null) old.internalDestroy();

		Bukkit.getPluginManager().registerEvents(this, plugin);

		this.repeatTask = plugin.runAsyncTimerTask(() -> {
			PlatformBridge.get().sendActionBar(player, prompt);
		}, 1);

		this.timeoutTask = plugin.runAsyncTaskLater(this::internalDestroy, builder.timeoutSeconds);
	}

	public static boolean isWaitingForInput(Player player) {
		return SESSIONS.containsKey(player.getUniqueId());
	}

	private void internalDestroy() {
		SESSIONS.remove(player.getUniqueId(), this);
		HandlerList.unregisterAll(this);
		if (repeatTask != null) repeatTask.cancel();
		if (timeoutTask != null) timeoutTask.cancel();
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

	public static Builder builder(Homestead plugin, Player player) {
		return new Builder(plugin, player);
	}

	public static final class Builder {
		private final Homestead plugin;
		private final Player player;
		private BiConsumer<Player, String> callback;
		private Function<String, Boolean> validator;
		private Consumer<Player> onCancel;
		private String prompt;
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

		public Builder prompt(String prompt) {
			this.prompt = prompt;
			return this;
		}

		public Builder prompt(int messagePath) {
			String key = String.valueOf(messagePath);

			this.prompt = Resources.<LanguageFile>get(ResourceType.Language).getString(key);

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

			if (prompt == null || prompt.isEmpty()) {
				throw new IllegalStateException("Prompt must be set");
			}

			return new PlayerInputSession(this);
		}
	}
}