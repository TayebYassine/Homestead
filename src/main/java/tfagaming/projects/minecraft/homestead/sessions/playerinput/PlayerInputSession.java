package tfagaming.projects.minecraft.homestead.sessions.playerinput;

import com.google.common.base.Function;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PlayerInputSession implements Listener {

	private static final Map<UUID, PlayerInputSession> SESSIONS = new ConcurrentHashMap<>();
	private static final Map<UUID, BukkitTask> TIMERS = new ConcurrentHashMap<>();

	private final Homestead plugin;
	private final Player player;
	private final BiConsumer<Player, String> callback;
	private final Function<String, Boolean> validator;
	private final Consumer<Player> onCancel;
	private final String prompt;
	private final BukkitTask repeatTask;
	private final BukkitTask timeoutTask;

	public PlayerInputSession(Homestead plugin,
							  Player player,
							  BiConsumer<Player, String> callback,
							  Function<String, Boolean> validator,
							  Consumer<Player> onCancel,
							  int messagePath) {

		this.plugin = plugin;
		this.player = player;
		this.callback = callback;
		this.validator = validator;
		this.onCancel = onCancel;

		String key = String.valueOf(messagePath);
		this.prompt = ChatColorTranslator.translate(Homestead.language.get(key));

		PlayerInputSession old = SESSIONS.put(player.getUniqueId(), this);
		if (old != null) old.internalDestroy();

		Bukkit.getPluginManager().registerEvents(this, plugin);

		repeatTask = plugin.runAsyncTimerTask(() -> {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
					new TextComponent(prompt));
		}, 1);

		timeoutTask = plugin.runAsyncTaskLater(this::internalDestroy, 60);
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

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if (!e.getPlayer().equals(player)) return;
		e.setCancelled(true);

		String msg = e.getMessage();
		if (msg.equalsIgnoreCase("cancel")) {

			plugin.runSyncTask(() -> {
				onCancel.accept(player);
				internalDestroy();
			});

			return;
		}

		if (validator.apply(msg)) {
			plugin.runSyncTask(() -> {
				callback.accept(player, msg);
				internalDestroy();
			});
		}
	}

	@EventHandler
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
}