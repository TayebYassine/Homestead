package tfagaming.projects.minecraft.homestead;

import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.commands.brigadier.BrigadierCommands;
import tfagaming.projects.minecraft.homestead.commands.operator.ForceUnclaimCommand;
import tfagaming.projects.minecraft.homestead.commands.operator.HomesteadAdminCommand;
import tfagaming.projects.minecraft.homestead.commands.standard.ClaimCommand;
import tfagaming.projects.minecraft.homestead.commands.standard.RegionCommand;
import tfagaming.projects.minecraft.homestead.commands.standard.UnclaimCommand;
import tfagaming.projects.minecraft.homestead.database.Database;
import tfagaming.projects.minecraft.homestead.database.Driver;
import tfagaming.projects.minecraft.homestead.database.cache.LevelsCache;
import tfagaming.projects.minecraft.homestead.database.cache.RegionsCache;
import tfagaming.projects.minecraft.homestead.database.cache.SubAreasCache;
import tfagaming.projects.minecraft.homestead.database.cache.WarsCache;
import tfagaming.projects.minecraft.homestead.events.MemberTaxes;
import tfagaming.projects.minecraft.homestead.events.RegionRent;
import tfagaming.projects.minecraft.homestead.events.RegionUpkeep;
import tfagaming.projects.minecraft.homestead.integrations.*;
import tfagaming.projects.minecraft.homestead.integrations.maps.RegionIconTools;
import tfagaming.projects.minecraft.homestead.listeners.*;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.sessions.AutoClaimSession;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.storage.StorageManager;
import tfagaming.projects.minecraft.homestead.tools.https.UpdateChecker;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.papermc.TaskHandle;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.IntegrationsUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Homestead extends JavaPlugin {
	private final static String VERSION = "5.1.0.0-26w15b";
	private final static boolean SNAPSHOT = true;
	public static Database database;
	public static RegionsCache regionsCache;
	public static WarsCache warsCache;
	public static SubAreasCache subAreasCache;
	public static LevelsCache levelsCache;
	public static Vault vault;
	private static Homestead INSTANCE;
	private static long STARTED_AT;
	private static TaskHandle moveCheckTask;

	public static String getVersion() {
		return VERSION;
	}

	public static boolean isSnapshot() {
		return SNAPSHOT;
	}

	public static Homestead getInstance() {
		return INSTANCE;
	}

	public static boolean isExternalPluginEnabled(String pluginName) {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);

		return plugin != null && plugin.isEnabled();
	}

	public static boolean isFolia() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public void onEnable() {
		Homestead.INSTANCE = this;
		Homestead.STARTED_AT = System.currentTimeMillis();

		new Logger();

		try {
			if (!getDataFolder().exists()) {
				if (!getDataFolder().mkdirs()) {
					throw new IOException("Unable to create Bukkit data directory");
				}
			}

			prepareDataFolder("regions");
			prepareDataFolder("wars");
			prepareDataFolder("subareas");
			prepareDataFolder("levels");
		} catch (IOException | SecurityException e) {
			endInstance(e);
			return;
		}

		saveDefaultConfig();

		try {
			Resources.load(this);
		} catch (Exception e) {
			endInstance(e);
			return;
		}

		Homestead.regionsCache = new RegionsCache();
		Homestead.warsCache = new WarsCache();
		Homestead.subAreasCache = new SubAreasCache();
		Homestead.levelsCache = new LevelsCache();

		try {
			Driver provider = Driver.parse(Resources.<ConfigFile>get(ResourceType.Config).getDatabaseProvider());

			if (provider == null) {
				throw new IllegalStateException("Database provider not found.");
			}

			Homestead.database = new Database(provider);
		} catch (Exception e) {
			endInstance(e);
			return;
		}

		try {
			database.importToCache();
		} catch (Exception e) {
			endInstance(e);
			return;
		}

		if (!IntegrationsUtils.isVaultInstalled()) {
			Logger.error("Unable to start the plugin; \"Vault\" is required. Shutting down plugin instance...");

			if (isFolia()) {
				Logger.error("FOLIA DETECTED! USE VaultUnlocked INSTEAD OF Vault! THE ORIGINAL VERSION DOESN'T SUPPORT FOLIA!");
			}

			endInstance();
			return;
		} else {
			Logger.warning("Loading service providers with Vault... (Using " + (!isFolia() ? "Legacy Vault" : "VaultUnlocked") + ")");
		}

		StorageManager.init(this);

		Homestead.vault = new Vault(this);

		if (!Homestead.vault.setupEconomy()) {
			Logger.warning("No Economy service provider found.");
			Logger.warning("Any feature requiring an Economy service will be skipped.");
		} else {
			Logger.info("Loaded service provider: Economy [" + Homestead.vault.getEconomy().getName() + "]");
		}

		if (!Homestead.vault.setupPermissions()) {
			if (Limits.getLimitsMethod() == Limits.LimitMethod.GROUPS) {
				Logger.error("No Permissions service provider found.");
				Logger.error("You are using groups as a limit method, and permission services are required for Homestead to run. Shutting down plugin instance...");
				endInstance();
				return;
			} else {
				Logger.warning("No Permission service provider found.");
				Logger.warning("The plugin is using static permissions; operator and non-operator.");
			}
		} else {
			Logger.info("Loaded service provider: Permissions [" + Homestead.vault.getPermissions().getPermissionsName() + "]");
		}

		if (Resources.<ConfigFile>get(ResourceType.Config).getBoolean("clean-startup")) {
			RegionManager.cleanStartup();
			WarManager.cleanStartup();
			SubAreaManager.cleanStartup();
			LevelManager.cleanStartup();
			ChunkManager.deleteInvalidChunks();
		}

		ChunkManager.reregisterForceLoadedChunks();

		registerCommands();
		registerEvents();
		registerBrigadier();

		if (Resources.<ConfigFile>get(ResourceType.Config).getBoolean("metrics")) {
			new bStats(this);

			Logger.info("bStats metrics is enabled, anonymous data is being sent to the servers.");

			try {
				new FastStats(this);

				Logger.info("FastStats metrics is enabled, anonymous data is being sent to the servers.");
			} catch (Exception e) {
				Logger.error(e);
			}
		}

			Logger.debug("Debug mode is enabled in config.yml; logs.txt may be flooded with warnings.");

		Logger.info("Ready, took " + (System.currentTimeMillis() - STARTED_AT) + " ms to load.");

		// Cache interval
		int cacheInterval = Resources.<ConfigFile>get(ResourceType.Config).getCacheInterval();

		runAsyncTimerTask(() -> {
			try {
				Homestead.database.exportFromCache();
			} catch (Exception e) {
				endInstance(e);
			}
		}, 10, cacheInterval);

		// Download icons
		if (Resources.<ConfigFile>get(ResourceType.Config).getBoolean("dynamic-maps.icons.enabled")) {
			runAsyncTask(() -> {
				Logger.warning("Downloading required web map render icons... This may take a while!");
				RegionIconTools.downloadAllIcons();
				Logger.info("Successfully downloaded all icons!");
			});
		}

		// Triggers
		runAsyncTimerTask(() -> {
			DynamicMaps.trigger(this);
		}, Resources.<ConfigFile>get(ResourceType.Config).getInt("dynamic-maps.update-interval"));

		if (Homestead.vault.isEconomyReady() && Resources.<ConfigFile>get(ResourceType.Config).getBoolean("taxes.enabled")) {
			runAsyncTimerTask(() -> {
				MemberTaxes.trigger(this);
			}, 10);
		}

		if (Homestead.vault.isEconomyReady() && Resources.<ConfigFile>get(ResourceType.Config).getBoolean("upkeep.enabled")) {
			runAsyncTimerTask(() -> {
				RegionUpkeep.trigger(this);
			}, 10);
		}

		if (Homestead.vault.isEconomyReady() && Resources.<ConfigFile>get(ResourceType.Config).getBoolean("renting.enabled")) {
			runAsyncTimerTask(() -> {
				RegionRent.trigger(this);
			}, 10);
		}

		// Check for updates every 24 hours
		runAsyncTimerTask(() -> {
			UpdateChecker.fetch(this);
		}, 86400);

		// Register external plugins
		registerExternalPlugins();

		// Do NOT touch this one
		if (ItemTransportingEntityValidateTargetListener.isClassFound()) {
			Logger.debug("Event [ItemTransportingEntityValidateTargetListener] found, using PaperMC built-in event for Copper Golems interaction");

			getServer().getPluginManager().registerEvents(new ItemTransportingEntityValidateTargetListener(), this);
		} else {
			Logger.debug("Event [ItemTransportingEntityValidateTargetListener] not found, using alternative method with Entities Moving listener");

			if (!isFolia()) {
				moveCheckTask = new TaskHandle(Bukkit.getScheduler().runTaskTimer(this, () -> {
					for (World world : Bukkit.getWorlds()) {
						for (Entity entity : world.getEntities()) {
							RegionProtectionListener.onEntityMove(entity);
						}
					}
				}, 0L, 1L));
			}
		}
	}

	private void registerCommands() {
		CommandBuilder.register(new RegionCommand());
		CommandBuilder.register(new ClaimCommand());
		CommandBuilder.register(new UnclaimCommand());

		CommandBuilder.register(new HomesteadAdminCommand());
		CommandBuilder.register(new ForceUnclaimCommand());
	}

	private void registerEvents() {
		try {
			getServer().getPluginManager().registerEvents(new PlayerRegionEnterAndExitListener(), this);
		} catch (Exception e) {
			Logger.error(e);
		}
		try {
			getServer().getPluginManager().registerEvents(new RegionProtectionListener(), this);
		} catch (Exception e) {
			Logger.error(e);
		}
		try {
			getServer().getPluginManager().registerEvents(new SelectionToolListener(), this);
		} catch (Exception e) {
			Logger.error(e);
		}
		try {
			getServer().getPluginManager().registerEvents(new CommandsCooldownListener(), this);
		} catch (Exception e) {
			Logger.error(e);
		}
		try {
			getServer().getPluginManager().registerEvents(new CustomSignsListener(), this);
		} catch (Exception e) {
			Logger.error(e);
		}
		try {
			getServer().getPluginManager().registerEvents(new PlayerAutoClaimListener(), this);
		} catch (Exception e) {
			Logger.error(e);
		}
		try {
			getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
		} catch (Exception e) {
			Logger.error(e);
		}
		try {
			getServer().getPluginManager().registerEvents(new BorderBreakListener(), this);
		} catch (Exception e) {
			Logger.error(e);
		}
		try {
			getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
		} catch (Exception e) {
			Logger.error(e);
		}
		try {
			getServer().getPluginManager().registerEvents(new DelayedTeleportListener(), this);
		} catch (Exception e) {
			Logger.error(e);
		}

		try {
			getServer().getPluginManager().registerEvents(new PlayerEnterEndExitPortalListener(), this);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private void registerBrigadier() {
		try {
			if (CommodoreProvider.isSupported()) {
				Commodore commodore = CommodoreProvider.getCommodore(this);
				new BrigadierCommands(this, commodore);
			} else {
				Logger.warning("Mojang Brigadier is not supported on this server software.");
			}
		} catch (NoClassDefFoundError e) {
			Logger.warning("Commodore/Brigadier classes not present. Skipping Brigadier command registration.");
		}
	}

	/**
	 * Run a task on the region thread that owns the given player.
	 * Use this instead of runSyncTask() whenever the task involves world/chunk/location
	 * access triggered by a player action (e.g. inventory clicks).
	 *
	 * @param player   The player whose region thread to run on.
	 * @param callable The task to run.
	 */
	public TaskHandle runPlayerTask(Player player, Runnable callable) {
		if (isFolia()) {
			return new TaskHandle(player.getScheduler().run(this, task -> callable.run(), null));
		}

		return new TaskHandle(Bukkit.getScheduler().runTask(this, callable));
	}

	/**
	 * Run a repeating task asynchronously with interval in seconds.
	 *
	 * @param callable The task to run.
	 * @param interval The interval, in seconds.
	 */
	public TaskHandle runAsyncTimerTask(Runnable callable, int interval) {
		if (isFolia()) {
			return new TaskHandle(Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> callable.run(), 0, interval, TimeUnit.SECONDS));
		}

		long intervalTicks = interval * 20L;

		return new TaskHandle(Bukkit.getScheduler().runTaskTimerAsynchronously(this, callable, 0L, intervalTicks));
	}

	/**
	 * Run a repeating task synchronously with interval in ticks.
	 *
	 * @param callable The task to run.
	 * @param ticks    The interval, in ticks.
	 */
	public TaskHandle runSyncTimerTask(Runnable callable, long ticks) {
		if (isFolia()) {
			// Folia requires initial delay >= 1; global region scheduler uses ticks
			return new TaskHandle(Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> callable.run(), 1L, ticks));
		}

		return new TaskHandle(Bukkit.getScheduler().runTaskTimer(this, callable, 0L, ticks));
	}

	/**
	 * Run a task synchronously after a delay in seconds.
	 *
	 * @param callable The task to run.
	 * @param delay    The delay, in seconds.
	 */
	public TaskHandle runSyncTaskLater(Runnable callable, int delay) {
		if (isFolia()) {
			long delayTicks = delay * 20L;
			return new TaskHandle(Bukkit.getGlobalRegionScheduler().runDelayed(this, task -> callable.run(), delayTicks));
		}

		long delayTicks = delay * 20L;

		return new TaskHandle(Bukkit.getScheduler().runTaskLater(this, callable, delayTicks));
	}

	/**
	 * Run a repeating task asynchronously with interval in seconds, with a delay in seconds.
	 *
	 * @param callable The task to run.
	 * @param interval The interval, in seconds.
	 */
	public TaskHandle runAsyncTimerTask(Runnable callable, int delay, int interval) {
		if (isFolia()) {
			return new TaskHandle(Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> callable.run(), delay, interval, TimeUnit.SECONDS));
		}

		long delayTicks = delay * 20L;
		long intervalTicks = interval * 20L;

		return new TaskHandle(Bukkit.getScheduler().runTaskTimerAsynchronously(this, callable, delayTicks, intervalTicks));
	}

	/**
	 * Run a task asynchronously after a delay in seconds.
	 *
	 * @param callable The task to run.
	 * @param delay    The delay, in seconds.
	 */
	public TaskHandle runAsyncTaskLater(Runnable callable, int delay) {
		if (isFolia()) {
			return new TaskHandle(Bukkit.getAsyncScheduler().runDelayed(this, task -> callable.run(), delay, TimeUnit.SECONDS));
		}

		long delayTicks = delay * 20L;

		return new TaskHandle(Bukkit.getScheduler().runTaskLaterAsynchronously(this, callable, delayTicks));
	}

	/**
	 * Run a task asynchronously.
	 *
	 * @param callable The task to run.
	 */
	public TaskHandle runAsyncTask(Runnable callable) {
		if (isFolia()) {
			return new TaskHandle(Bukkit.getAsyncScheduler().runNow(this, task -> callable.run()));
		}

		return new TaskHandle(Bukkit.getScheduler().runTaskAsynchronously(this, callable));
	}

	/**
	 * Run a task synchronously.
	 *
	 * @param callable The task to run.
	 */
	public TaskHandle runSyncTask(Runnable callable) {
		if (isFolia()) {
			return new TaskHandle(Bukkit.getGlobalRegionScheduler().run(this, task -> callable.run()));
		}

		return new TaskHandle(Bukkit.getScheduler().runTask(this, callable));
	}

	/**
	 * Run a task on the region thread that owns the given location.
	 * Use this for any event or operation tied to a specific world location.
	 *
	 * @param location The location whose owning region thread to run on.
	 * @param callable The task to run.
	 */
	public TaskHandle runLocationTask(Location location, Runnable callable) {
		if (isFolia()) {
			return new TaskHandle(Bukkit.getRegionScheduler().run(this, location, task -> callable.run()));
		}

		return new TaskHandle(Bukkit.getScheduler().runTask(this, callable));
	}

	/**
	 * Runs a repeating task on the region scheduler with given location.
	 *
	 * @param location The location to determine which region thread to use
	 * @param callable The task to run
	 * @param delay Ticks to wait before first execution
	 * @param period Ticks between executions
	 */
	public TaskHandle runLocationTaskTimer(Location location, Runnable callable, long delay, long period) {
		if (isFolia()) {
			return new TaskHandle(Bukkit.getRegionScheduler().runAtFixedRate(this, location, task -> callable.run(), delay, period));
		}

		return new TaskHandle(Bukkit.getScheduler().runTaskTimer(this, callable, delay, period));
	}

	/**
	 * Get a list of offline players.
	 */
	public List<OfflinePlayer> getOfflinePlayersSync() {
		OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();

		return Arrays.asList(offlinePlayers);
	}

	/**
	 * Get an offline player with player unique IDs, using safe method.
	 *
	 * @param playerId The player ID.
	 */
	public OfflinePlayer getOfflinePlayerSync(UUID playerId) {
		Player onlinePlayer = Bukkit.getPlayer(playerId);

		if (onlinePlayer != null) {
			return onlinePlayer;
		}

		OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();

		for (OfflinePlayer player : offlinePlayers) {
			if (player.getName() != null && player.hasPlayedBefore() && player.getUniqueId().equals(playerId)) {
				return player;
			}
		}

		return null;
	}

	/**
	 * Get an offline player with player name, using safe method.
	 *
	 * @param playerName The player name.
	 */
	public OfflinePlayer getOfflinePlayerSync(String playerName) {
		Player onlinePlayer = Bukkit.getPlayer(playerName);

		if (onlinePlayer != null) {
			return onlinePlayer;
		}

		OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();

		for (OfflinePlayer player : offlinePlayers) {
			if (player.getName() != null && player.hasPlayedBefore() && player.getName().equals(playerName)) {
				return player;
			}
		}

		return null;
	}

	public void onDisable() {
		if (database != null) {
			Logger.info("Closing database connection...");

			try {
				database.closeConnection();
			} catch (Exception ignored) {
			}
		}

		if (moveCheckTask != null) {
			moveCheckTask.cancel();
		}

		Logger.warning("Saving storage for each region...");

		StorageManager.saveAll();

		Logger.warning("Cleaning cache...");

		Homestead.regionsCache.clear();
		Homestead.warsCache.clear();
		Homestead.subAreasCache.clear();
		Homestead.levelsCache.clear();
		TargetRegionSession.sessions.clear();
		AutoClaimSession.sessions.clear();
		DelayedTeleport.cleanup();

		Logger.info("Cache cleaned.");

		Logger.info("Homestead has been disabled. Goodbye!");
	}

	private void prepareDataFolder(String dirName) throws IOException {
		File dir = new File(getDataFolder(), dirName);

		if (!dir.exists() && !dir.mkdir()) {
			throw new IOException("Unable to create '" + dirName + "' directory, path: " + dir.getAbsolutePath());
		}
	}

	public void registerExternalPlugins() {
		if (isExternalPluginEnabled("PlaceholderAPI")) {
			boolean placeholderRegistered = new PlaceholderAPI().register();

			if (!placeholderRegistered) {
				Logger.error("Failed to register hooks.");
			}
		}
	}

	/**
	 * Kill the plugin's instance.
	 */
	public void endInstance() {
		getServer().getPluginManager().disablePlugin(this);
	}

	public void endInstance(Throwable e) {
		Logger.error(e);

		endInstance();
	}
}
