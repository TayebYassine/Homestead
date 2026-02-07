package tfagaming.projects.minecraft.homestead;

import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.commands.MojangBrigadier;
import tfagaming.projects.minecraft.homestead.commands.commands.*;
import tfagaming.projects.minecraft.homestead.config.ConfigLoader;
import tfagaming.projects.minecraft.homestead.config.LanguageLoader;
import tfagaming.projects.minecraft.homestead.config.MenusConfigLoader;
import tfagaming.projects.minecraft.homestead.database.*;
import tfagaming.projects.minecraft.homestead.events.MemberTaxes;
import tfagaming.projects.minecraft.homestead.events.RegionRent;
import tfagaming.projects.minecraft.homestead.events.RegionUpkeep;
import tfagaming.projects.minecraft.homestead.integrations.DynamicMaps;
import tfagaming.projects.minecraft.homestead.integrations.PlaceholderAPI;
import tfagaming.projects.minecraft.homestead.integrations.Vault;
import tfagaming.projects.minecraft.homestead.integrations.bStats;
import tfagaming.projects.minecraft.homestead.integrations.maps.RegionIconTools;
import tfagaming.projects.minecraft.homestead.listeners.*;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.LevelsManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.managers.WarsManager;
import tfagaming.projects.minecraft.homestead.sessions.autoclaim.AutoClaimSession;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.https.UpdateChecker;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.IntegrationsUtils;
import tfagaming.projects.minecraft.homestead.tools.validator.YAMLValidator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class Homestead extends JavaPlugin {
	private final static String VERSION = "5.0.0.0";
	private final static boolean SNAPSHOT = false;
	public static Database database;
	public static RegionsCache regionsCache;
	public static WarsCache warsCache;
	public static SubAreasCache subAreasCache;
	public static LevelsCache levelsCache;
	public static ConfigLoader config;
	public static LanguageLoader language;
	public static MenusConfigLoader menusConfig;
	public static Vault vault;
	private static Homestead INSTANCE;
	private static long startedAt;

	private static BukkitTask moveCheckTask;

	public static String getVersion() {
		return VERSION;
	}

	public static boolean isSnapshot() {
		return SNAPSHOT;
	}

	public static Homestead getInstance() {
		return INSTANCE;
	}

	public void onEnable() {
		Homestead.INSTANCE = this;
		Homestead.startedAt = System.currentTimeMillis();

		new Logger();

		try {
			if (!getDataFolder().exists()) {
				if (!getDataFolder().mkdirs()) {
					throw new IOException("Unable to create Bukkit data directory");
				}
			}

			File regionsFolder = new File(getDataFolder(), "regions");
			if (!regionsFolder.exists()) {
				if (!regionsFolder.mkdir()) {
					throw new IOException("Unable to create regions directory");
				}
			}

			File warsFolder = new File(getDataFolder(), "wars");
			if (!warsFolder.exists()) {
				if (!warsFolder.mkdir()) {
					throw new IOException("Unable to create wars directory");
				}
			}

			File subAreasFolder = new File(getDataFolder(), "subareas");
			if (!subAreasFolder.exists()) {
				if (!subAreasFolder.mkdir()) {
					throw new IOException("Unable to create subareas directory");
				}
			}
		} catch (IOException | SecurityException e) {
			endInstance(e);
			return;
		}

		saveDefaultConfig();

		config = new ConfigLoader(this);

		language = new LanguageLoader(this, config.get("language"));

		menusConfig = new MenusConfigLoader(this);

		Set<String> skipKeys = new HashSet<>();

		YAMLValidator configValidator = new YAMLValidator("config.yml", new File(getDataFolder(), "config.yml"),
				skipKeys);

		try {
			if (!configValidator.validate()) {
				configValidator.fix();

				config = new ConfigLoader(this);
			}
		} catch (IOException e) {
			endInstance(e);
			return;
		}

		YAMLValidator languageValidator = new YAMLValidator("en-US.yml", language.getLanguageFile(config.get("language")));

		try {
			if (!languageValidator.validate()) {
				languageValidator.fix();

				language = new LanguageLoader(this, config.get("language"));
			}
		} catch (IOException e) {
			endInstance(e);
			return;
		}

		YAMLValidator menusConfigValidator = new YAMLValidator("menus.yml", new File(getDataFolder(), "menus.yml"),
				skipKeys);

		try {
			if (!menusConfigValidator.validate()) {
				menusConfigValidator.fix();

				menusConfig = new MenusConfigLoader(this);
			}
		} catch (IOException e) {
			endInstance(e);
			return;
		}

		Homestead.regionsCache = new RegionsCache(config.get("cache-interval"));
		Homestead.warsCache = new WarsCache(config.get("cache-interval"));
		Homestead.subAreasCache = new SubAreasCache(config.get("cache-interval"));
		Homestead.levelsCache = new LevelsCache(config.get("cache-interval"));

		try {
			Database.Provider provider = Database.parseProviderFromString(config.get("database.provider"));

			if (provider == null) {
				throw new IllegalStateException("Database provider not found.");
			}

			Homestead.database = new Database(provider);
		} catch (ClassNotFoundException | SQLException | IOException e) {
			endInstance(e);
			return;
		}

		database.importRegions();
		database.importWars();
		database.importSubAreas();
		database.importLevels();

		if (!IntegrationsUtils.isVaultInstalled()) {
			Logger.error("Unable to start the plugin; \"Vault\" is required. Shutting down plugin instance...");
			endInstance();
			return;
		} else {
			Logger.warning("Loading service providers with Vault...");
		}

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
			Logger.info("Loaded service provider: Permissions [" + Homestead.vault.getPermissions().getName() + "]");
		}

		if ((boolean) Homestead.config.get("clean-startup")) {
			RegionsManager.cleanStartup();
			WarsManager.cleanStartup();
			SubAreasManager.cleanStartup();
			LevelsManager.cleanStartup();
		}

		registerCommands();
		registerEvents();
		registerBrigadier();

		if ((boolean) Homestead.config.get("metrics")) {
			new bStats(this);

			Logger.info("bStats metrics is enabled, anonymous data is being sent every 30 minutes.");
		}

		if (Homestead.config.isDebugEnabled()) {
			Logger.warning("Debug mode is enabled in config.yml; logs.txt may be flooded with warnings.");
		}

		Logger.info("Ready, took " + (System.currentTimeMillis() - startedAt) + " ms to load.");

		if ((boolean) Homestead.config.get("dynamic-maps.icons.enabled")) {
			runAsyncTask(() -> {
				Logger.warning("Downloading required web map render icons... This may take a while!");
				RegionIconTools.downloadAllIcons();
				Logger.info("Successfully downloaded all icons!");
			});
		}

		runAsyncTimerTask(() -> {
			runAsyncTask(() -> {
				new DynamicMaps(this);
			});
		}, Homestead.config.get("dynamic-maps.update-interval"));

		if (Homestead.vault.isEconomyReady() && (boolean) Homestead.config.get("taxes.enabled")) {
			runAsyncTimerTask(() -> {
				new MemberTaxes(this);
			}, 10);
		}

		if (Homestead.vault.isEconomyReady() && (boolean) Homestead.config.get("upkeep.enabled")) {
			runAsyncTimerTask(() -> {
				new RegionUpkeep(this);
			}, 10);
		}

		if (Homestead.vault.isEconomyReady() && (boolean) Homestead.config.get("renting.enabled")) {
			runAsyncTimerTask(() -> {
				new RegionRent(this);
			}, 10);
		}

		runAsyncTimerTask(() -> {
			runAsyncTask(() -> {
				UpdateChecker.check(this);
			});
		}, 86400);

		registerExternalPlugins();

		moveCheckTask = runSyncTimerTask(() -> {
			for (World world : Bukkit.getWorlds()) {
				for (Entity entity : world.getEntities()) {
					RegionProtectionListener.onEntityMove(entity);
				}
			}
		}, 5L);
	}

	private void registerCommands() {
		CommandBuilder.register(new RegionCommand());
		CommandBuilder.register(new ClaimCommand());
		CommandBuilder.register(new UnclaimCommand());
		CommandBuilder.register(new HomesteadAdminCommand());
		CommandBuilder.register(new ForceUnclaimCommand());
	}

	private void registerEvents() {
		getServer().getPluginManager().registerEvents(new PlayerRegionEnterAndExitListener(), this);
		getServer().getPluginManager().registerEvents(new RegionProtectionListener(), this);
		getServer().getPluginManager().registerEvents(new SelectionToolListener(), this);
		getServer().getPluginManager().registerEvents(new CommandsCooldownListener(), this);
		getServer().getPluginManager().registerEvents(new CustomSignsListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerAutoClaimListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
		getServer().getPluginManager().registerEvents(new BorderBreakListener(), this);
		getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
		getServer().getPluginManager().registerEvents(new DelayedTeleportListener(), this);
	}

	private void registerBrigadier() {
		try {
			if (CommodoreProvider.isSupported()) {
				Commodore commodore = CommodoreProvider.getCommodore(this);
				new MojangBrigadier(this, commodore);
			} else {
				Logger.warning("Mojang Brigadier is not supported on this server software.");
			}
		} catch (NoClassDefFoundError e) {
			Logger.warning("Commodore/Brigadier classes not present. Skipping Brigadier command registration.");
		}
	}

	/**
	 * Run a repeating task asynchronously with interval in seconds.
	 *
	 * @param callable The task to run.
	 * @param interval The interval, in seconds.
	 */
	public BukkitTask runAsyncTimerTask(Runnable callable, int interval) {
		long intervalTicks = interval * 20L;

		return Bukkit.getScheduler().runTaskTimerAsynchronously(this, callable, 0L, intervalTicks);
	}

	/**
	 * Run a repeating task synchronously with interval in ticks.
	 *
	 * @param callable The task to run.
	 * @param ticks    The interval, in ticks.
	 */
	public BukkitTask runSyncTimerTask(Runnable callable, long ticks) {
		return Bukkit.getScheduler().runTaskTimer(this, callable, 0L, ticks);
	}

	/**
	 * Run a task synchronously after a delay in seconds.
	 *
	 * @param callable The task to run.
	 * @param delay    The delay, in seconds.
	 */
	public BukkitTask runSyncTaskLater(Runnable callable, int delay) {
		long delayTicks = delay * 20L;

		return Bukkit.getScheduler().runTaskLater(this, callable, delayTicks);
	}

	/**
	 * Run a repeating task asynchronously with interval in seconds, with a delay in seconds.
	 *
	 * @param callable The task to run.
	 * @param interval The interval, in seconds.
	 */
	public void runAsyncTimerTask(Runnable callable, int delay, int interval) {
		long delayTicks = delay * 20L;
		long intervalTicks = interval * 20L;

		Bukkit.getScheduler().runTaskTimerAsynchronously(this, callable, delayTicks, intervalTicks);
	}

	/**
	 * Run a task asynchronously after a delay in seconds.
	 *
	 * @param callable The task to run.
	 * @param delay    The delay, in seconds.
	 */
	public BukkitTask runAsyncTaskLater(Runnable callable, int delay) {
		long delayTicks = delay * 20L;

		return Bukkit.getScheduler().runTaskLaterAsynchronously(this, callable, delayTicks);
	}

	/**
	 * Run a task asynchronously.
	 *
	 * @param callable The task to run.
	 */
	public void runAsyncTask(Runnable callable) {
		Bukkit.getScheduler().runTaskAsynchronously(this, callable);
	}

	/**
	 * Run a task synchronously.
	 *
	 * @param callable The task to run.
	 */
	public void runSyncTask(Runnable callable) {
		Bukkit.getScheduler().runTask(this, callable);
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
			database.closeConnection();
		}

		if (moveCheckTask != null) {
			moveCheckTask.cancel();
		}

		Logger.warning("Cleaning cache...");

		Homestead.regionsCache.clear();
		Homestead.warsCache.clear();
		TargetRegionSession.sessions.clear();
		AutoClaimSession.sessions.clear();

		Logger.info("Cache cleaned.");

		Logger.info("Homestead has been disabled. Goodbye!");
	}

	public void registerExternalPlugins() {
		if (isExternalPluginEnabled("PlaceholderAPI")) {
			boolean placeholderRegistered = new PlaceholderAPI(this).register();

			if (!placeholderRegistered) {
				Logger.error("Failed to register hooks.");
			}
		}
	}

	public boolean isExternalPluginEnabled(String pluginName) {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);

		return plugin != null && plugin.isEnabled();
	}

	/**
	 * Kill the plugin's instance.
	 */
	public void endInstance() {
		getServer().getPluginManager().disablePlugin(this);
	}

	public void endInstance(Throwable e) {
		Logger.error(e);

		getServer().getPluginManager().disablePlugin(this);
	}
}
